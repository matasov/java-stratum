package strat.mining.stratum.proxy.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresqlManager {

  private static Connection connection;

  public static Connection getConnection() throws IOException, SQLException {
    System.out.println(PostgresqlManager.class.getResource("/config/postgresql.properties"));
    if (connection == null) {
      Properties prop = new Properties();
      String propFileName = "/config/postgresql.properties";
      InputStream inputStream = PostgresqlManager.class.getResource(propFileName).openStream();
      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException(
            "property file '" + propFileName + "' not found in the classpath");
      }
      connection = DriverManager.getConnection(prop.getProperty("url"), prop);
    }
    return connection;
  }
}
