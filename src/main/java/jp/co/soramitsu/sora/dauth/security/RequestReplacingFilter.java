package jp.co.soramitsu.sora.dauth.security;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RequestReplacingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getHeader(CONTENT_TYPE).startsWith("multipart")) {
      log.trace("Passing request without changes");
      filterChain.doFilter(request, response);
      return;
    }
    log.trace("Replacing request with wrapper");
    filterChain.doFilter(new RequestWrapper(request), response);
  }
}
