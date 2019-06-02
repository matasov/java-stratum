package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.model.SiteUser;

public interface SiteUserRepository {

  void addSiteUser(SiteUser siteUser) throws SQLException, IOException;

  void removeSiteUser(SiteUser siteUser) throws SQLException, IOException;

  void updateSiteUser(SiteUser siteUser) throws SQLException, IOException;

  SiteUser getSiteUserByID(UUID siteUserID) throws SQLException, IOException;

  SiteUser getSiteUserBySession(String siteUserSession) throws SQLException, IOException;
  
  SiteUser getSiteUserByName(String siteUserName) throws SQLException, IOException;

  List<SiteUser> getLoginingSiteUsers() throws SQLException, IOException;
  
}
