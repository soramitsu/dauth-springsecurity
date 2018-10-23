package jp.co.soramitsu.sora.dauth;

import static java.lang.Long.parseLong;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static jp.co.soramitsu.sora.dauth.DAuthHeaders.SORA_AUTH_ID;
import static jp.co.soramitsu.sora.dauth.DAuthHeaders.SORA_AUTH_PUBLIC_KEY;
import static jp.co.soramitsu.sora.dauth.DAuthHeaders.SORA_AUTH_SIGNATURE;
import static jp.co.soramitsu.sora.dauth.DAuthHeaders.SORA_AUTH_TIMESTAMP;
import static jp.co.soramitsu.sora.sdk.did.model.dto.DID.parse;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.resolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import jp.co.soramitsu.sora.sdk.did.model.dto.DID;
import jp.co.soramitsu.sora.sdk.did.parser.generated.ParserException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.DecoderException;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Slf4j
public class DAuthToken extends UsernamePasswordAuthenticationToken {

  private DID authId;
  private Instant authTimestamp;
  private DID publicKeyId;
  private String signatureHex;
  private HttpMethod method;
  private String postParams;
  private String requestUrl;

  public DAuthToken(HttpServletRequest request) {
    super(request.getHeader(SORA_AUTH_ID), null);
    try {
      initFields(request);
    } catch (IOException e) {
      throw new BadCredentialsException("Reading request body exception", e);
    } catch (ParserException e) {
      log.warn(
          "Can't parse {} or {}, message: {}, request not authenticated",
          SORA_AUTH_ID,
          SORA_AUTH_PUBLIC_KEY,
          e.getMessage());
      throw new BadCredentialsException("Can't parse", e);
    } catch (Throwable e) {
      throw new BadCredentialsException("Incorrect credentials", e);
    }
  }

  private void initFields(HttpServletRequest request) throws ParserException, IOException {
    val authHeader = request.getHeader(SORA_AUTH_ID);
    val timestampHeader = request.getHeader(SORA_AUTH_TIMESTAMP);
    val publicKeyHeader = request.getHeader(SORA_AUTH_PUBLIC_KEY);
    val signatureHeader = request.getHeader(SORA_AUTH_SIGNATURE);

    log.trace("Incoming request header: {}: {}",
        SORA_AUTH_ID, authHeader
    );
    log.trace("Incoming request header: {}: {}",
        SORA_AUTH_TIMESTAMP, timestampHeader
    );
    log.trace("Incoming request header: {}: {}",
        SORA_AUTH_PUBLIC_KEY, publicKeyHeader
    );
    log.trace("Incoming request header: {}: {}",
        SORA_AUTH_SIGNATURE, signatureHeader
    );

    authId = parse(authHeader);
    authTimestamp = ofEpochMilli(parseLong(timestampHeader));

    log.trace("Auth timestamp in UTC format: {}", authTimestamp);

    publicKeyId = parse(publicKeyHeader);
    signatureHex = base64ToHex(signatureHeader);

    log.trace("Signature hex: {}", signatureHex);

    method =
        ofNullable(resolve(request.getMethod().toUpperCase()))
            .orElseThrow(
                () -> new IllegalArgumentException("Got null instead of request method"));

    log.trace("Request method: {}", method);

    try (BufferedReader reader = request.getReader()) {
      readRequestBody(reader);
    }
    requestUrl = getFullURL(request);

    log.trace("Request full url: {}", requestUrl);
  }

  private void readRequestBody(BufferedReader reader) {
    postParams =
        method.equals(POST) || method.equals(PUT)
            ? reader.lines().collect(joining(lineSeparator()))
            : "";
    log.trace("Request body: {}", postParams);
  }

  private static String getFullURL(HttpServletRequest request) {
    StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());

    log.trace("Request URL: {}", requestURL);

    String queryString = request.getQueryString();

    log.trace("Request query string: {}", queryString);

    if (queryString == null) {
      return requestURL.toString();
    }

    return requestURL.append('?').append(queryString).toString();
  }

  private static String base64ToHex(String base64String) {
    return printHexBinary(parseBase64Binary(base64String));
  }

  public String payload() {
    return method + requestUrl + postParams + meta();
  }

  byte[] payloadBytes() {
    return payload().getBytes(UTF_8);
  }

  private String meta() {
    return authTimestamp.toEpochMilli() + authId.toString() + publicKeyId;
  }

  byte[] signatureBytes() {
    try {
      return decodeHex(signatureHex);
    } catch (DecoderException e) {
      log.warn("Can't decode hex: {}", signatureHex);
      throw new IllegalArgumentException(e);
    }
  }
}
