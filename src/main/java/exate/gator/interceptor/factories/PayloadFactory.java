package exate.gator.interceptor.factories;

import exate.gator.interceptor.api.ApiConfig;
import exate.gator.interceptor.api.DatasetPayload;
import exate.gator.interceptor.api.TokenPayload;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PayloadFactory {
    private PayloadFactory() {}

    public static TokenPayload createTokenPayload(ApiConfig config) {
        return new TokenPayload(config.clientId(), config.clientSecret(), config.grantType());
    }

    public static DatasetPayload createDatasetPayload(ApiConfig config, HttpResponse<Buffer> targetResponse) {
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
            targetResponse.bodyAsString(),
            config.preserveStringLength(),
            config.sqlType().orElse(null),
            config.classificationModel().orElse(null));
    }
}
