package exate.gator.interceptor.services;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.mockito.BDDMockito.given;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.content.DatasetPayload;
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
    @Mock
    GatorConfig gator;
    @InjectMocks PayloadsServiceImpl sut;

    @Test
    void test_token_payload_factory() {
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

        @Test
        void test_dataset_payload_factory_with_minimal_config() {
            given(gator.manifestName()).willReturn("fake-manifest");
            given(gator.jobType()).willReturn(DatasetPayload.JobType.Encrypt);
            given(gator.countryCode()).willReturn("GB");
            given(gator.protectNullValues()).willReturn(false);
            given(gator.preserveStringLength()).willReturn(true);

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
        void test_dataset_payload_factory_with_full_config() {
            given(gator.manifestName()).willReturn("fake-manifest");
            given(gator.jobType()).willReturn(DatasetPayload.JobType.Encrypt);
            given(gator.countryCode()).willReturn("GB");
            given(gator.protectNullValues()).willReturn(false);
            given(gator.preserveStringLength()).willReturn(true);

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
