package jp.co.soramitsu.sora.dauth.services;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(PRIVATE)
@FieldDefaults(level = PRIVATE)
public class GenericResponse {

  Status status;

  @Data
  @Setter(PRIVATE)
  @AllArgsConstructor
  @NoArgsConstructor
  @FieldDefaults(level = PRIVATE)
  public static class Status {

    String code;
    String message;
  }
}
