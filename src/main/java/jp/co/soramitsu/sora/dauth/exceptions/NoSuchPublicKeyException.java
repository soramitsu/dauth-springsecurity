package jp.co.soramitsu.sora.dauth.exceptions;

import jp.co.soramitsu.sora.sdk.did.model.dto.DID;
import org.springframework.security.authentication.BadCredentialsException;

public class NoSuchPublicKeyException extends BadCredentialsException {

  public NoSuchPublicKeyException(DID publicKeyId) {
    super("Public key with id: " + publicKeyId + " not found");
  }

}
