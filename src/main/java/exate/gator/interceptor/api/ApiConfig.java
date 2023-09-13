package exate.gator.interceptor.api;

import exate.gator.interceptor.constraints.CountryCode;
import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

@ConfigMapping(prefix = "api.gator")
public interface ApiConfig {
    @NotBlank
    String host();

    @NotNull
    Integer port();

    @NotBlank
    String datasetUri();

    @NotBlank
    String tokenUri();

    @NotBlank
    String apiKey();

    @NotBlank
    String clientId();

    @NotBlank
    String clientSecret();

    @NotBlank
    String grantType();

    @NotBlank
    String manifestName();

    @NotNull
    DatasetPayload.JobType jobType();

    Optional<String> thirdPartyName();

    Optional<Integer> thirdPartyId();

    @NotBlank
    @CountryCode.CountryCodeString
    String countryCode();

    @CountryCode.CountryCodeOptional
    Optional<String> dataOwningCountryCode();

    Optional<Integer> dataUsageId();

    @NotNull
    Boolean protectNullValues();

    Optional<String> restrictedText();

    @NotNull
    Boolean preserveStringLength();

    Optional<DatasetPayload.SqlType> sqlType();

    Optional<String> classificationModel();
}
