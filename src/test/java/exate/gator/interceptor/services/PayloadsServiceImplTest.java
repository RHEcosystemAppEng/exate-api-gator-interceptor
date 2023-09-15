package exate.gator.interceptor.services;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.BDDMockito.*;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.content.DatasetPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PayloadsServiceImplTest {
    @Mock GatorConfig gator;
    @InjectMocks PayloadsServiceImpl sut;

    @Test
    void create_a_token_payload_and_verify_expected_to_string_implementation() {
        given(gator.clientId()).willReturn("dummy-client-id");
        given(gator.clientSecret()).willReturn("dummy-client-secret");
        given(gator.grantType()).willReturn("dummy-grant-type");

        var payload = sut.createTokenPayload();

        then(payload.toString()) // unique toString implementation should contain the mocks values in its structure
            .isEqualTo("client_id=dummy-client-id&client_secret=dummy-client-secret&grant_type=dummy-grant-type");
    }

    @Nested
    class TestDatasetFactory {
        String dataset = "{\"this_is\": \"a_fake_dataset\"}";

        @BeforeEach
        void initialize() {
            when(gator.manifestName()).thenReturn("fake-manifest");
            when(gator.jobType()).thenReturn(DatasetPayload.JobType.Encrypt);
            when(gator.countryCode()).thenReturn("GB");
            when(gator.protectNullValues()).thenReturn(false);
            when(gator.preserveStringLength()).thenReturn(true);
        }

        @Test
        void create_a_dataset_payload_with_only_the_mandatory_configuration_and_verify() {
            var payload = sut.createDatasetPayload(dataset);

            then(payload.dataSet()).isEqualTo(dataset);
            then(payload.manifestName()).isEqualTo("fake-manifest");
            then(payload.jobType()).isEqualTo(DatasetPayload.JobType.Encrypt);
            then(payload.countryCode()).isEqualTo("GB");
            then(payload.protectNullValues()).isFalse();
            then(payload.preserveStringLength()).isTrue();

            thenNoException().isThrownBy(() -> ZonedDateTime.parse(payload.snapshotDate()));
        }

        @Test
        void create_a_dataset_payload_with_a_full_configuration_and_verify() {
            given(gator.thirdPartyName()).willReturn(Optional.of("fake-third-party-name"));
            given(gator.thirdPartyId()).willReturn(Optional.of(98));
            given(gator.dataOwningCountryCode()).willReturn(Optional.of("GB"));
            given(gator.dataUsageId()).willReturn(Optional.of(89));
            given(gator.restrictedText()).willReturn(Optional.of("****"));
            given(gator.sqlType()).willReturn(Optional.of(DatasetPayload.SqlType.Dremio));
            given(gator.classificationModel()).willReturn(Optional.of("fake-classification-model"));

            var payload = sut.createDatasetPayload(dataset);

            then(payload.thirdPartyIdentifer().thirdPartyName()).isEqualTo("fake-third-party-name");
            then(payload.thirdPartyIdentifer().thirdPartyId()).isEqualTo(98);
            then(payload.dataOwningCountryCode()).isEqualTo("GB");
            then(payload.dataUsageId()).isEqualTo(89);
            then(payload.restrictedText()).isEqualTo("****");
            then(payload.sqlType()).isEqualTo(DatasetPayload.SqlType.Dremio);
            then(payload.classificationModel()).isEqualTo("fake-classification-model");
        }
    }
}
