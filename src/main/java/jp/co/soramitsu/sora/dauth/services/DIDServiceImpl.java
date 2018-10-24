package jp.co.soramitsu.sora.dauth.services;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.cache.CacheLoader.from;
import static java.lang.String.format;
import static java.time.Duration.ofMinutes;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static jp.co.soramitsu.sora.dauth.utils.Utils.restTemplate;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import jp.co.soramitsu.sora.dauth.exceptions.DidExistsException;
import jp.co.soramitsu.sora.dauth.exceptions.UnknownResponseException;
import jp.co.soramitsu.sora.sdk.did.model.dto.DDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class DIDServiceImpl implements DIDService {

  private static final String GET_DDO_FORMAT = "%s/did/%s";
  private static final String POST_DDO_FORMAT = "%s/did";
  private URL didResolverUrl;
  private RestTemplate restTemplate = restTemplate();
  private LoadingCache<String, DDO> ddoCache;

  @Builder
  public DIDServiceImpl(@NonNull URL didResolverUrl, Duration refreshAfterWriteDuration,
      RestTemplate restTemplate) {
    this.didResolverUrl = didResolverUrl;
    if (nonNull(restTemplate)) {
      this.restTemplate = restTemplate;
    }

    var refreshDuration = ofMinutes(1);
    if (nonNull(refreshAfterWriteDuration)) {
      refreshDuration = refreshAfterWriteDuration;
    }

    ddoCache = newBuilder()
        .refreshAfterWrite(refreshDuration)
        .build(loader());
  }

  private CacheLoader<String, DDO> loader() {
    return from(
        did -> {
          try {
            log.debug("Trying to fetch DDO by DID {}", did);
            val response = requestGetDDO(did);
            if (response.getStatusCode().is2xxSuccessful()) {
              log.debug("DDO {} fetched successfully by DID {}", response.getBody(), did);
              return response.getBody();
            }
            return null;
          } catch (ResourceAccessException e) {
            throw new IllegalStateException("Wrong DID resolver url, or it's DOWN!!!");
          }
        });
  }

  @Override
  public Optional<DDO> getDDO(@NonNull String did) {
    try {
      return ofNullable(ddoCache.get(did));
    } catch (ExecutionException e) {
      log.warn("Error while fetching DDO by DID: {}, message: {}", did, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void postDDO(@NonNull DDO ddo) throws DidExistsException {
    val response = requestCreateDDO(ddo);
    if (response.getStatusCode() != OK) {
      if (response.getStatusCode() == UNPROCESSABLE_ENTITY) {
        throw new DidExistsException(ddo.getId());
      } else {
        throw new UnknownResponseException(response.getStatusCode());
      }
    }
  }

  private ResponseEntity<Void> requestCreateDDO(DDO ddo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    HttpEntity<DDO> entity = new HttpEntity<>(ddo, headers);

    return restTemplate.exchange(format(POST_DDO_FORMAT, didResolverUrl), POST, entity, Void.class);
  }

  private ResponseEntity<DDO> requestGetDDO(@Nullable String key) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    HttpEntity entity = new HttpEntity(headers);

    return restTemplate.exchange(
        format(GET_DDO_FORMAT, didResolverUrl, key), GET, entity, DDO.class);
  }
}
