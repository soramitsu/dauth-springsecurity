package jp.co.soramitsu.sora.dauth;

import java.util.Optional;
import java.util.function.Function;
import jp.co.soramitsu.sora.dauth.exceptions.InvalidSignatureException;
import jp.co.soramitsu.sora.dauth.exceptions.NoSuchAuthenticationException;
import jp.co.soramitsu.sora.dauth.exceptions.NoSuchDIDException;
import jp.co.soramitsu.sora.dauth.exceptions.NoSuchPublicKeyException;
import jp.co.soramitsu.sora.sdk.did.model.dto.DDO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@Slf4j
@AllArgsConstructor
public class DAuthProvider implements AuthenticationProvider {

  private Function<String, Optional<DDO>> didService;
  /**
   * Invoked on successful authorization. MUST produce {@link Authentication} with {@link
   * Authentication#isAuthenticated()} value true
   */
  private Function<DAuthToken, Authentication> onSuccess;

  private boolean isValidSignature(DAuthToken token) {
    log.debug("Fetching DDO...");

    val ddo = didService
        .apply(token.getAuthId().toString())
        .orElseThrow(() -> new NoSuchDIDException(token.getAuthId()));

    log.trace("Fetched DDO: {}", ddo);

    val verifyingExecutor = new VerifyingExecutor();

    ddo.getAuthentication().stream()
        .filter(auth -> auth.getPublicKey().equals(token.getPublicKeyId())).findFirst()
        .orElseThrow(() -> new NoSuchAuthenticationException(token.getPublicKeyId()))
        .execute(verifyingExecutor);

    log.trace("Executed authentication logic");

    ddo.getPublicKey().stream()
        .filter(pk -> pk.getId().equals(token.getPublicKeyId())).findFirst()
        .orElseThrow(() -> new NoSuchPublicKeyException(token.getPublicKeyId()))
        .execute(verifyingExecutor);

    log.trace("Executed public key logic");

    return verifyingExecutor.verify(
        token.payloadBytes(),
        token.signatureBytes()
    );
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    try {
      val dAuthentication = (DAuthToken) authentication;
      if (isValidSignature(dAuthentication)) {
        log.debug("Successful authorization by DID: {}", dAuthentication.getAuthId());
        return onSuccess.apply(dAuthentication);
      }

      log.debug(
          "DID {} tried to authorize, but signature hex: {} is invalid, according to public key: {}",
          dAuthentication.getAuthId(),
          dAuthentication.getSignatureHex(),
          dAuthentication.getPublicKeyId());
      throw new InvalidSignatureException(dAuthentication);
    } catch (RuntimeException e) {
      log.warn("Unexpected runtime exception, message: {}", e.getMessage());
      throw new BadCredentialsException("Unexpected exception", e);
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(DAuthToken.class);
  }

}
