package exate.gator.interceptor.factories;

import exate.gator.interceptor.configs.ApiConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.TokenPayload;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Use the functions in this static class for creating payload to be used in API-Gator requests. */
public class PayloadFactory {
    private PayloadFactory() {}

    /**
     * Create a payload for TOKEN requests to API-Gator.
     * @param config ApiConfig instance for creating the payload with its properties.
     * @return a payload ready to be sent for TOKEN requests.
     */
    public static TokenPayload createTokenPayload(ApiConfig config) {
        return new TokenPayload(config.clientId(), config.clientSecret(), config.grantType());
    }

    /**
     * Create a payload for DATASET requests to API-Gator.
     * @param config ApiConfig instance for creating the payload with.
     * @param dataset the dataset is the body from the original service response required to be Gator'ed.
     * @return a payload ready to be sent for DATASET requests.
     */
    public static DatasetPayload createDatasetPayload(ApiConfig config, String dataset) {
        return new DatasetPayload(
            config.manifestName(),
            config.jobType(),
            config.thirdPartyName().isPresent() || config.thirdPartyId().isPresent()
                ? new DatasetPayload.ThirdPartyIdentiferPayload(
                config.thirdPartyName().orElse(null), config.thirdPartyId().orElse(null))
                : null,
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            config.dataOwningCountryCode().orElse(null),
            config.countryCode(),
            config.dataUsageId().orElse(null),
            config.protectNullValues(),
            config.restrictedText().orElse(null),
            dataset,
            config.preserveStringLength(),
            config.sqlType().orElse(null),
            config.classificationModel().orElse(null));
    }
}
