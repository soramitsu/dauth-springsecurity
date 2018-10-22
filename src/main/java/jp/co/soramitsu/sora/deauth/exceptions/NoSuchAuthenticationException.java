package jp.co.soramitsu.sora.deauth.exceptions;

import jp.co.soramitsu.sora.sdk.did.model.dto.DID;
import org.springframework.security.authentication.BadCredentialsException;

public class NoSuchAuthenticationException extends BadCredentialsException {

  public NoSuchAuthenticationException(DID publicKeyId) {
    super("Authentication with Public Key id: " + publicKeyId + " not found");
  }
}
