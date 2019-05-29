package jp.co.soramitsu.sora.dauth.security;

import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RequestReplacingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    val contentType = request.getHeader(CONTENT_TYPE);
    if (nonNull(contentType) && contentType.startsWith(MULTIPART_FORM_DATA_VALUE)) {
      log.trace("Passing request without changes");
      filterChain.doFilter(request, response);
      return;
    }
    log.trace("Replacing request with wrapper");
    filterChain.doFilter(new RequestWrapper(request), response);
  }
}
