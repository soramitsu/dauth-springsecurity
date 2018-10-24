package jp.co.soramitsu.sora.dauth.exceptions;

import org.springframework.http.HttpStatus;

public class UnknownResponseException extends RuntimeException {

  public UnknownResponseException(HttpStatus status) {
    super("Returned unexpected http status: " + status);
  }
}
