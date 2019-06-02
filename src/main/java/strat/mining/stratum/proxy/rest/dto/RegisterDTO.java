package strat.mining.stratum.proxy.rest.dto;

import lombok.Data;

@Data
public class RegisterDTO {

  private String name;
  
  private String password;
  
  private String repeatPassword;
  
}
