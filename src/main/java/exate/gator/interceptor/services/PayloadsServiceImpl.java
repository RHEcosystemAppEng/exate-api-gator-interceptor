package exate.gator.interceptor.services;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.TokenPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PayloadsServiceImpl implements PayloadsService {
    private final GatorConfig gator;

    @Inject
    public PayloadsServiceImpl(GatorConfig gator) {
        this.gator = gator;
    }

    public TokenPayload createTokenPayload() {
        return new TokenPayload(this.gator.clientId(), this.gator.clientSecret(), this.gator.grantType());
    }

    public DatasetPayload createDatasetPayload(String dataset) {
        return new DatasetPayload(
            this.gator.manifestName(),
            this.gator.jobType(),
            this.gator.thirdPartyName().isPresent() || this.gator.thirdPartyId().isPresent()
                ? new DatasetPayload.ThirdPartyIdentiferPayload(
                this.gator.thirdPartyName().orElse(null), this.gator.thirdPartyId().orElse(null))
                : null,
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            this.gator.dataOwningCountryCode().orElse(null),
            this.gator.countryCode(),
            this.gator.dataUsageId().orElse(null),
            this.gator.protectNullValues(),
            this.gator.restrictedText().orElse(null),
            dataset,
            this.gator.preserveStringLength(),
            this.gator.sqlType().orElse(null),
            this.gator.classificationModel().orElse(null));
    }
}
