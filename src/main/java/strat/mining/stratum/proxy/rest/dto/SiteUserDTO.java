package strat.mining.stratum.proxy.rest.dto;

import java.util.Date;
import java.util.UUID;
import lombok.Data;
import strat.mining.stratum.proxy.database.model.SiteUser;
import strat.mining.stratum.proxy.database.model.SiteUserImplemented;

@Data
public class SiteUserDTO {
  private UUID id;

  private String name;

  private String password;

  private Date creationTime;

  private String session;

  private Date timeSession;

  public SiteUser getSiteUser() {
    SiteUserImplemented result = new SiteUserImplemented();
    result.setId(id);
    result.setName(name);
    result.setPassword(password);
    result.setCreationTime(creationTime);
    result.setSession(session);
    result.setTimeSession(timeSession);
    return result;
  }
}
