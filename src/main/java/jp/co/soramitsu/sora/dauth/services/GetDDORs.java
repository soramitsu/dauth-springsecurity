package jp.co.soramitsu.sora.dauth.services;

import static lombok.AccessLevel.PRIVATE;

import jp.co.soramitsu.sora.sdk.did.model.dto.DDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@Setter(PRIVATE)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE)
public class GetDDORs extends GenericResponse {

  DDO ddo;

  public GetDDORs() {
    super(new Status("OK", "Mock successful DidResolver response"));
  }

  public GetDDORs(DDO ddo) {
    super(new Status("OK", "Mock successful DidResolver response"));
    this.ddo = ddo;
  }
}
