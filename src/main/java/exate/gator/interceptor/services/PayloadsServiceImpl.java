package exate.gator.interceptor.services;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.TokenPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
            this.gator.thirdParty().isEmpty()
                ? null
                : new DatasetPayload.ThirdPartyIdentiferPayload(
                    this.gator.thirdParty().get().name().orElse(null),
                    this.gator.thirdParty().get().id().orElse(null)),
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
            this.gator.dataOwningCountryCode().orElse(null),
            this.gator.countryCode(),
            this.gator.dataUsageId().orElse(null),
            this.gator.protectNullValues(),
            this.gator.restrictedText().orElse(null),
            dataset,
            this.gator.preserveStringLength(),
            this.gator.sqlType().orElse(null),
            this.gator.classificationModel().orElse(null),
            this.gator.matchingrule().isEmpty() || this.gator.matchingrule().get().claims().isEmpty()
                ? null
                : new DatasetPayload.MatchingRule(this.gator.matchingrule().get().claims()));
    }
}
