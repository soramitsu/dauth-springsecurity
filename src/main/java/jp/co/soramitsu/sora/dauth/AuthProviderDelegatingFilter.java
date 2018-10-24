package jp.co.soramitsu.sora.dauth;

import java.io.IOException;
import java.util.function.Function;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Generic authenticating filter which uses passed function to map request to some authentication
 *
 * <p>After that, {@link Authentication} is passed to registered {@link
 * org.springframework.security.authentication.AuthenticationManager}
 */
public class AuthProviderDelegatingFilter extends AbstractAuthenticationProcessingFilter {

  private Function<HttpServletRequest, Authentication> authenticationFunction;

  public AuthProviderDelegatingFilter(
      RequestMatcher requiresAuth,
      Function<HttpServletRequest, Authentication> authenticationFunction) {
    super(requiresAuth);
    this.authenticationFunction = authenticationFunction;
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authResult);
    chain.doFilter(request, response);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    return getAuthenticationManager().authenticate(authenticationFunction.apply(request));
  }
}
