package strat.mining.stratum.proxy.database.model;

import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
public class SiteUserImplemented implements SiteUser {

  private UUID id;

  private String name;

  private String password;

  private Date creationTime;

  private String session;

  private Date timeSession;

}
