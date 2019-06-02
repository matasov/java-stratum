package strat.mining.stratum.proxy.database.model;

import java.util.Date;
import java.util.UUID;

public interface SiteUser {

  UUID getId();

  String getName();

  String getPassword();

  Date getCreationTime();

  String getSession();

  Date getTimeSession();
}
