package jp.co.soramitsu.sora.dauth.exceptions;

import jp.co.soramitsu.sora.sdk.did.model.dto.DID;
import org.springframework.security.authentication.BadCredentialsException;

public class NoSuchDIDException extends BadCredentialsException {

  public NoSuchDIDException(DID did) {
    super("DDO by DID: " + did + " not found!");
  }
}
