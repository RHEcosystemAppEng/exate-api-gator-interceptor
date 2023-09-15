package exate.gator.interceptor.services;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.BDDMockito.*;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.configs.TargetConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.RequestHeaders;
import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptionsServiceImplTest {
    @Mock GatorConfig gator;

    @Mock TargetConfig target;

    @InjectMocks OptionsServiceImpl sut;

    MultiMap fakeHeaders;

    @BeforeEach
    void initialize() {
        fakeHeaders = MultiMap.caseInsensitiveMultiMap();
        fakeHeaders.add("fake-header", "fake-value");
        fakeHeaders.add("another-fake-header", "another-fake-value");
    }

    @Test
    void create_target_request_options_and_verify(@Mock HttpServerRequest targetRequest) {
        given(target.host()).willReturn("dummy-target-host");
        given(target.port()).willReturn(4321);

        given(targetRequest.uri()).willReturn("/fake/target/endpoint");
        given(targetRequest.headers()).willReturn(fakeHeaders);

        var opts = sut.createTargetOptions(targetRequest);

        assertThat(opts.getHost()).isEqualTo("dummy-target-host");
        assertThat(opts.getPort()).isEqualTo(4321);
        assertThat(opts.getURI()).isEqualTo("/fake/target/endpoint");

        var optsHeaders = opts.getHeaders();
        assertThat(optsHeaders.size()).isEqualTo(2);
        assertThat(optsHeaders.get("fake-header")).isEqualTo("fake-value");
        assertThat(optsHeaders.get("another-fake-header")).isEqualTo("another-fake-value");
    }

    @Nested
    class TestTokenOptionsCreation {
        @BeforeEach
        void initialize() {
            when(gator.host()).thenReturn("fake-gator-host");
            when(gator.port()).thenReturn(9987);
            when(gator.tokenUri()).thenReturn("/fake/token/endpoint");
            lenient().when(gator.apiKey()).thenReturn("fake-config-api-key");
        }

        @Test
        void create_token_request_options_with_apikey_only_in_config_and_verify() {
            var opts = sut.createTokenOptions(fakeHeaders);

            assertThat(opts.getHost()).isEqualTo("fake-gator-host");
            assertThat(opts.getPort()).isEqualTo(9987);
            assertThat(opts.getURI()).isEqualTo("/fake/token/endpoint");

            var optsHeaders = opts.getHeaders();
            assertThat(optsHeaders.size()).isEqualTo(2);
            assertThat(optsHeaders.get(RequestHeaders.X_Api_Key.toString())).isEqualTo("fake-config-api-key");
            assertThat(optsHeaders.get(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/x-www-form-urlencoded");
        }

        @Test
        void create_token_request_options_with_apikey_in_config_and_header_and_verify_header_usage() {
            fakeHeaders.add(RequestHeaders.X_Api_Key.toString(), "fake-headers-api-key");
            var opts = sut.createTokenOptions(fakeHeaders);

            assertThat(opts.getHost()).isEqualTo("fake-gator-host");
            assertThat(opts.getPort()).isEqualTo(9987);
            assertThat(opts.getURI()).isEqualTo("/fake/token/endpoint");

            var optsHeaders = opts.getHeaders();
            assertThat(optsHeaders.size()).isEqualTo(2);
            assertThat(optsHeaders.get(RequestHeaders.X_Api_Key.toString())).isEqualTo("fake-headers-api-key");
            assertThat(optsHeaders.get(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/x-www-form-urlencoded");
        }
    }

    @Nested
    class TestDatasetOptionsCreation {
        @Mock TokenResponse tokenResponse;

        @BeforeEach
        void initialize() {
            when(gator.host()).thenReturn("fake-dataset-gator-host");
            when(gator.port()).thenReturn(4455);
            when(gator.datasetUri()).thenReturn("/fake/dataset/endpoint");
            when(gator.apiKey()).thenReturn("fake-config-api-key");
            when(gator.datasetType()).thenReturn(DatasetPayload.DatasetType.JSON);

            when(tokenResponse.token_type()).thenReturn("bearer");
            when(tokenResponse.access_token()).thenReturn("fake-access-token");
        }
        @Test
        void create_dataset_request_options_without_overriding_headers_and_verify() {
            var opts = sut.createDatasetOptions(tokenResponse, fakeHeaders);

            assertThat(opts.getHost()).isEqualTo("fake-dataset-gator-host");
            assertThat(opts.getPort()).isEqualTo(4455);
            assertThat(opts.getURI()).isEqualTo("/fake/dataset/endpoint");

            var optsHeaders = opts.getHeaders();
            assertThat(optsHeaders.size()).isEqualTo(4);
            assertThat(optsHeaders.get(RequestHeaders.X_Api_Key.toString())).isEqualTo("fake-config-api-key");
            assertThat(optsHeaders.get(RequestHeaders.X_Resource_Token.toString())).isEqualTo("bearer fake-access-token");
            assertThat(optsHeaders.get(RequestHeaders.X_Data_Set_Type.toString())).isEqualTo(DatasetPayload.DatasetType.JSON.name());
            assertThat(optsHeaders.get(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");
        }

        @Test
        void create_dataset_request_options_overriding_headers_with_the_ones_from_the_target_request_and_verify() {
            // overriding existing
            fakeHeaders.add(RequestHeaders.X_Data_Set_Type.toString(), DatasetPayload.DatasetType.SQL.name());
            // including new
            fakeHeaders.add(RequestHeaders.X_Validation_Key.toString(), "dummy-validation-key");
            fakeHeaders.add(RequestHeaders.X_Execution_Context.toString(), "dummy-execution-context");
            fakeHeaders.add(RequestHeaders.X_Dataset_On_Invalid_Manifest.toString(), "true");
            fakeHeaders.add(RequestHeaders.X_Silent_Mode.toString(), "false");

            var opts = sut.createDatasetOptions(tokenResponse, fakeHeaders);

            assertThat(opts.getHost()).isEqualTo("fake-dataset-gator-host");
            assertThat(opts.getPort()).isEqualTo(4455);
            assertThat(opts.getURI()).isEqualTo("/fake/dataset/endpoint");

            var optsHeaders = opts.getHeaders();
            assertThat(optsHeaders.size()).isEqualTo(8);
            assertThat(optsHeaders.get(RequestHeaders.X_Api_Key.toString())).isEqualTo("fake-config-api-key");
            assertThat(optsHeaders.get(RequestHeaders.X_Resource_Token.toString())).isEqualTo("bearer fake-access-token");
            assertThat(optsHeaders.get(RequestHeaders.X_Data_Set_Type.toString())).isEqualTo(DatasetPayload.DatasetType.SQL.name());
            assertThat(optsHeaders.get(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");

            assertThat(optsHeaders.get(RequestHeaders.X_Validation_Key.toString())).isEqualTo("dummy-validation-key");
            assertThat(optsHeaders.get(RequestHeaders.X_Execution_Context.toString())).isEqualTo("dummy-execution-context");
            assertThat(optsHeaders.get(RequestHeaders.X_Dataset_On_Invalid_Manifest.toString())).isEqualTo("true");
            assertThat(optsHeaders.get(RequestHeaders.X_Silent_Mode.toString())).isEqualTo("false");
        }
    }
}
