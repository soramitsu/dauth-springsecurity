package jp.co.soramitsu.sora.dauth.services;

import java.util.Optional;
import java.util.function.Function;
import jp.co.soramitsu.sora.dauth.exceptions.DidExistsException;
import jp.co.soramitsu.sora.sdk.did.model.dto.DDO;
import lombok.NonNull;

/**
 * Default implementation will use DIDs provided with configurations, but this interface allows us
 * to implement other variant in future with dynamic DIDs lookup.
 */
public interface DIDService extends Function<String, Optional<DDO>> {

  @Override
  default Optional<DDO> apply(String s) {
    return getDDO(s);
  }

  /**
   * Returns DDO by the given DID. Implementation may use cache, so the DDO may not be up-to-date
   *
   * @param did - valid DID for searching DDO
   * @return DDO found
   */
  Optional<DDO> getDDO(String did);

  /**
   * Registers DDO in DID resolver.
   *
   * @param ddo valid DDO
   * @throws DidExistsException if such DDO already registered
   */
  void postDDO(@NonNull DDO ddo) throws DidExistsException;
}
