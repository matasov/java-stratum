package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.PostgresqlManager;
import strat.mining.stratum.proxy.database.model.SiteUser;
import strat.mining.stratum.proxy.database.model.SiteUserImplemented;

public class SiteUserRepositoryImplemented implements SiteUserRepository {

  private static String SITE_USER_TBL = "site_user";

  @Override
  public void addSiteUser(SiteUser siteUser) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("insert into %1$s values ('%2$s', '%3$s', '%4$s', '%5$s', '%6$s', '%7$s')",
            SITE_USER_TBL, siteUser.getId(), siteUser.getName(), siteUser.getPassword(),
            siteUser.getCreationTime(), siteUser.getSession(), siteUser.getTimeSession());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void removeSiteUser(SiteUser siteUser) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("delete from %1$s where id = '%2$s'", SITE_USER_TBL, siteUser.getId());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updateSiteUser(SiteUser siteUser) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "update %1$s set name = '%3$s', password = '%4$s', creation_time = '%5$s', session = '%6$s', time_session = '%7$s' where id = '%2$s'",
        SITE_USER_TBL, siteUser.getId(), siteUser.getName(), siteUser.getPassword(),
        siteUser.getCreationTime(), siteUser.getSession(), siteUser.getTimeSession());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public SiteUser getSiteUserByID(UUID siteUserID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", SITE_USER_TBL, siteUserID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        SiteUserImplemented row = new SiteUserImplemented();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setName(rs.getString("name"));
        row.setPassword(rs.getString("password"));
        row.setCreationTime(rs.getDate("creation_time"));
        row.setSession(rs.getString("session"));
        row.setTimeSession(rs.getDate("time_session"));
        return row;
      }
      rs.close();
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public SiteUser getSiteUserBySession(String siteUserSession) throws SQLException, IOException {
    if (siteUserSession == null || siteUserSession.equals("null")) {
      return null;
    }
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where session = '%2$s' and time_session > now()",
        SITE_USER_TBL, siteUserSession);

    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        SiteUserImplemented row = new SiteUserImplemented();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setName(rs.getString("name"));
        row.setPassword(rs.getString("password"));
        row.setCreationTime(rs.getDate("creation_time"));
        row.setSession(rs.getString("session"));
        row.setTimeSession(rs.getDate("time_session"));
        return row;
      }
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public SiteUser getSiteUserByName(String siteUserName) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("select * from %1$s where name = LOWER('%2$s')", SITE_USER_TBL, siteUserName);

    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        SiteUserImplemented row = new SiteUserImplemented();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setName(rs.getString("name"));
        row.setPassword(rs.getString("password"));
        row.setCreationTime(rs.getDate("creation_time"));
        row.setSession(rs.getString("session"));
        row.setTimeSession(rs.getDate("time_session"));
        return row;
      }
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public List<SiteUser> getLoginingSiteUsers() throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where time_session > now()", SITE_USER_TBL);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        List<SiteUser> resultSet = new ArrayList<>(rs.getFetchSize());
        while (rs.next()) {
          SiteUserImplemented row = new SiteUserImplemented();
          row.setId(UUID.fromString(rs.getString("id")));
          row.setName(rs.getString("name"));
          row.setPassword(rs.getString("password"));
          row.setCreationTime(rs.getDate("creation_time"));
          row.setSession(rs.getString("session"));
          row.setTimeSession(rs.getDate("time_session"));
          resultSet.add(row);
        }
        return resultSet;
      }
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

}
