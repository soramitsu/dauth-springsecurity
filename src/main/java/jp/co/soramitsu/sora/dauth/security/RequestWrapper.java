package jp.co.soramitsu.sora.dauth.security;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Wraps around real {@link HttpServletRequest} except that it immediately reads request body to
 * allow returning content repeatedly (default {@link HttpServletRequest} doesn't allow reading more
 * than once
 *
 * <p>Such a requirement is raised by Decentralized Authentication mechanism
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

  private byte[] contentBytes;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request The request to wrap
   * @throws IllegalArgumentException if the request is null
   */
  public RequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    val content = request.getReader().lines().collect(joining(lineSeparator()));
    this.contentBytes = content.getBytes(UTF_8);
  }

  /**
   * Required to allow reading same InputStream from request more than once
   *
   * <p>Such a requirement is raised by Decentralized Authentication mechanism
   */
  @Override
  public ServletInputStream getInputStream() {
    return new DelegatingServletInputStream(
        new ByteArrayInputStream(contentBytes));
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }
}
