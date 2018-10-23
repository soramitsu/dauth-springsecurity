package jp.co.soramitsu.sora.dauth;

import static java.util.Objects.nonNull;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.publicKeyFromBytes;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import java.security.PublicKey;
import java.util.function.BiFunction;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.sora.sdk.did.model.dto.authentication.AuthenticationVisitor;
import jp.co.soramitsu.sora.sdk.did.model.dto.authentication.Ed25519Sha3Authentication;
import jp.co.soramitsu.sora.sdk.did.model.dto.publickey.Ed25519Sha3VerificationKey;
import jp.co.soramitsu.sora.sdk.did.model.dto.publickey.PublicKeyVisitor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is visitor which allows to ease way of verifying signatures using {@link
 * jp.co.soramitsu.sora.sdk.did.model.dto.Authentication} and {@link
 * jp.co.soramitsu.sora.sdk.did.model.dto.PublicKey}
 *
 * Typical usage: {@code val verifyingVisitor = new VerifyingVisitor();
 * authentication.visit(verifyingVisitor); publicKey.visit(verifyingVisitor); // ready for verifying
 * using specific public key from last visit boolean result = verifyingVisitor.verify(payloadHash,
 * signatureHash); }
 *
 * Be aware that authentication and publicKey MUST be consistent (of same type)
 *
 * @apiNote Not thread-safe class
 */
@Slf4j
public class VerifyingVisitor implements AuthenticationVisitor, PublicKeyVisitor {

  private final Ed25519Sha3 ed = new Ed25519Sha3();
  /**
   * Using {@link java.util.function.Function} to abstract away from actual verification mechanism
   * (Ed25519Sha3 for instance)
   */
  private BiFunction<byte[], byte[], Boolean> verificationFunction;
  private PublicKey publicKey;

  @Override
  public void visit(Ed25519Sha3Authentication ed25519Sha3Authentication) {
    log.debug("Instantiating verifying function for type: {}", Ed25519Sha3Authentication.class);
    verificationFunction = (payloadHash, signatureHash) -> {
      //NOTE: if we will use algorithms different from Ed25519 for signature verification, then this method will be changed accordingly
      if (nonNull(publicKey)) {
        log.debug("Verifying payload {} against signature {}",
            encodeHexString(payloadHash),
            encodeHexString(signatureHash)
        );
        return ed.rawVerify(payloadHash, signatureHash, publicKey);
      } else {
        log.warn("Public key is absent!");
        throw new IllegalStateException(
            "PublicKey must visit this Visitor before using #verify method");
      }
    };
  }

  @Override
  public void visit(Ed25519Sha3VerificationKey ed25519Sha3VerificationKey) {
    log.trace("Public key: {}", encodeHexString(ed25519Sha3VerificationKey.getPublicKey()));
    this.publicKey = publicKeyFromBytes(ed25519Sha3VerificationKey.getPublicKey());
  }

  public boolean verify(byte[] payloadHash, byte[] signatureHash) {
    return verificationFunction.apply(payloadHash, signatureHash);
  }

}
