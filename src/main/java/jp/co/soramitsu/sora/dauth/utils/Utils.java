package jp.co.soramitsu.sora.dauth.utils;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class Utils {

  public static RestTemplate restTemplate() {
    val jacksonConverter = new MappingJackson2HttpMessageConverter();
    jacksonConverter.setSupportedMediaTypes(
        asList(APPLICATION_JSON, APPLICATION_JSON_UTF8, APPLICATION_OCTET_STREAM));
    val restTpl = new RestTemplate();
    restTpl.getMessageConverters().add(jacksonConverter);
    restTpl.setErrorHandler(new RestStatusHandler());
    return restTpl;
  }

  @Slf4j
  private static class RestStatusHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
      return !response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
      log.warn("Response from remote server: {} {}", response.getStatusCode(),
          response.getStatusText());
    }
  }

}
