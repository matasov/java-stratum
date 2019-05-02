/**
 * stratum-proxy is a proxy supporting the crypto-currency stratum pool mining
 * protocol.
 * Copyright (C) 2014-2015  Stratehm (stratehm@hotmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with multipool-stats-backend. If not, see <http://www.gnu.org/licenses/>.
 */
package strat.mining.stratum.proxy.utils;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Timer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);

	private static Timer instance;

	private ExecutorService executor;

	private Queue<Task> waitingTasks;

	private Scheduler scheduler;

	private Timer() {
		waitingTasks = new PriorityQueue<Task>(16, new Comparator<Task>() {
			public int compare(Task o1, Task o2) {
				int result = 0;
				if (o1 == null || o1.getExpectedExecutionTime() == null) {
					result = -1;
				} else if (o2 == null || o2.getExpectedExecutionTime() == null) {
					result = 1;
				} else {
					result = o1.getExpectedExecutionTime().compareTo(o2.getExpectedExecutionTime());
				}
				return result;
			}
		});
		scheduler = new Scheduler();
		executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("TimerExecutorThread-%s").setDaemon(true).build());

		Thread timerThread = new Thread(scheduler, "TimerSchedulerThread");
		timerThread.setDaemon(true);
		timerThread.start();
	}

	public static Timer getInstance() {
		if (instance == null) {
			instance = new Timer();
		}
		return instance;
	}

	/**
	 * Schedule the given task to execute in delay milliseconds.
	 * 
	 * @param task
	 * @param delay
	 */
	public void schedule(Task task, long delay) {
		// Check that the task is not null and delay is valid.
		if (task != null && delay >= 0) {
			LOGGER.debug("Scheduling of task {} in {} ms.", task.getName(), delay);
			task.setExpectedExecutionTime(System.currentTimeMillis() + delay);
			LOGGER.trace("Expected execution time of task {}: {}.", task.getName(), task.getExpectedExecutionTime());
			// Wake up the scheduler.
			synchronized (waitingTasks) {
				boolean isInserted = waitingTasks.offer(task);
				waitingTasks.notifyAll();
				if (isInserted) {
					LOGGER.trace("Task {} added in queue => Waking up the scheduler. {}", task.getName(), waitingTasks);
				} else {
					LOGGER.warn("Task {} not added in queue. {}", task.getName(), waitingTasks);
				}
			}
		} else {
			LOGGER.info("Failed to schedule task {} in {} ms.", task != null ? task.getName() : "null", delay);
		}
	}

	/**
	 * The scheduler of the timer.
	 * 
	 * @author Strat
	 * 
	 */
	protected class Scheduler implements Runnable {

		public void run() {
			while (true) {
				Task nextTask = null;
				long currentTime = System.currentTimeMillis();
				try {
					synchronized (waitingTasks) {
						LOGGER.trace("Looking for next task to execute: {}", waitingTasks);
						nextTask = waitingTasks.peek();

						if (nextTask == null || nextTask.getExpectedExecutionTime() > currentTime) {
							// Wait for a new task add if no task is present
							// or wait for the delay before the execution of the
							// next task.
							long timeToWait = 500;
							if (nextTask != null) {
								timeToWait = nextTask.getExpectedExecutionTime() - currentTime;
								LOGGER.trace("Next task to execute {}: waiting for {} ms.", nextTask.getName(), timeToWait);
							} else {
								LOGGER.trace("No task in the queue. Waiting for {} ms.", timeToWait);
							}

							waitingTasks.wait(timeToWait);

						} else {
							nextTask = waitingTasks.poll();

							LOGGER.trace("Task to execute now: {}.", nextTask.getName());

							// Run the task only if it is not cancelled.
							if (!nextTask.isCancelled()) {
								LOGGER.debug("Executing task {} now.", nextTask.getName());
								executor.execute(nextTask);
							} else {
								LOGGER.trace("Task {} cancelled. Do not execute.", nextTask.getName());
							}
						}
					}

				} catch (InterruptedException e) {
				} catch (Exception e) {
					LOGGER.error("Unexpected error in TimerSchedulerThread", e);
				}

			}
		}
	}

	/**
	 * A task that can be run by the {@link Timer}
	 * 
	 * @author Strat
	 * 
	 */
	public static abstract class Task implements Runnable {

		private static final AtomicLong taskCounter = new AtomicLong(0);

		volatile boolean isCancelled = false;

		private Long expectedExecutionTime;

		private String name;

		private Long taskId;

		public Task() {
			this.taskId = taskCounter.getAndIncrement();
		}

		public void cancel() {
			LOGGER.debug("Cancelling the task {}.", getName());
			isCancelled = true;
			synchronized (Timer.getInstance().waitingTasks) {
				boolean removed = Timer.getInstance().waitingTasks.remove(this);
				if (removed) {
					LOGGER.debug("Task {} removed. {}", getName(), Timer.getInstance().waitingTasks);
				} else {
					LOGGER.debug("Failed to remove task {} but still cancelled. {}", getName(), Timer.getInstance().waitingTasks);
				}
			}
		}

		public boolean isCancelled() {
			return isCancelled;
		}

		public Long getExpectedExecutionTime() {
			return expectedExecutionTime;
		}

		public void setExpectedExecutionTime(Long expectedExecutionTime) {
			this.expectedExecutionTime = expectedExecutionTime;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Task [isCancelled=");
			builder.append(isCancelled);
			builder.append(", expectedExecutionTime=");
			builder.append(expectedExecutionTime);
			builder.append(", name=");
			builder.append(name);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Task other = (Task) obj;
			if (taskId == null) {
				if (other.taskId != null)
					return false;
			} else if (!taskId.equals(other.taskId))
				return false;
			return true;
		}

	}

}
