package exate.gator.interceptor.services;

import exate.gator.interceptor.configs.ApiConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.TokenPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PayloadsServiceImpl implements PayloadsService {
    private final ApiConfig config;

    @Inject
    public PayloadsServiceImpl(ApiConfig config) {
        this.config = config;
    }

    public TokenPayload createTokenPayload() {
        return new TokenPayload(this.config.clientId(), this.config.clientSecret(), this.config.grantType());
    }

    public DatasetPayload createDatasetPayload(String dataset) {
        return new DatasetPayload(
            this.config.manifestName(),
            this.config.jobType(),
            this.config.thirdPartyName().isPresent() || this.config.thirdPartyId().isPresent()
                ? new DatasetPayload.ThirdPartyIdentiferPayload(
                this.config.thirdPartyName().orElse(null), this.config.thirdPartyId().orElse(null))
                : null,
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            this.config.dataOwningCountryCode().orElse(null),
            this.config.countryCode(),
            this.config.dataUsageId().orElse(null),
            this.config.protectNullValues(),
            this.config.restrictedText().orElse(null),
            dataset,
            this.config.preserveStringLength(),
            this.config.sqlType().orElse(null),
            this.config.classificationModel().orElse(null));
    }
}
