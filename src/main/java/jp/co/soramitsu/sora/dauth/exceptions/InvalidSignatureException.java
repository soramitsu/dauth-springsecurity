package jp.co.soramitsu.sora.dauth.exceptions;

import jp.co.soramitsu.sora.dauth.DAuthToken;
import org.springframework.security.authentication.BadCredentialsException;

public class InvalidSignatureException extends BadCredentialsException {

  public InvalidSignatureException(DAuthToken authToken) {
    super(
        "Can't verify payload: " + authToken.payload() + " against signature: " + authToken
            .getSignatureHex());
  }
}
