package exate.gator.interceptor.configs;

import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.constraints.CountryCode;
import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Configuration for API-Gator access. */
@ConfigMapping(prefix = "api.gator")
public interface GatorConfig {
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

    @NotNull
    DatasetPayload.DatasetType datasetType();

    @NotBlank
    String manifestName();

    @NotNull
    DatasetPayload.JobType jobType();

    Optional<ThirdParty> thirdParty();

    interface ThirdParty {
        Optional<String> name();
        Optional<Integer> id();
    }

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

    Optional<MatchingRule> matchingrule();

    interface MatchingRule {
        List<Map<String, String>> claims();
    }
}
