package jp.co.soramitsu.sora.dauth.exceptions;

import jp.co.soramitsu.sora.sdk.did.model.dto.DID;

public class DidExistsException extends Exception {

  public DidExistsException(DID did) {
    super("DID " + did + " already exists");
  }
}
