package exate.gator.interceptor.services;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.BDDMockito.*;

import exate.gator.interceptor.configs.TargetConfig;
import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestsServiceImplTest {
    WebClient client;
    @Mock OptionsService options;
    @Mock TargetConfig target;
    RequestsServiceImpl sut;
    @Mock RoutingContext context;
    @Mock HttpServerRequest originalRequest;

    @BeforeEach
    void initialize() {
        client = WebClient.create(mock(Vertx.class));
        when(context.request()).thenReturn(originalRequest);

        sut = new RequestsServiceImpl(client, options, target);
    }

    @Test
    void create_a_target_request_and_verify() {
        given(target.secure()).willReturn(true);
        given(originalRequest.method()).willReturn(HttpMethod.PUT);
        given(options.createTargetOptions(originalRequest)).willReturn(new RequestOptions());

        var request = sut.createTargetRequest(context);

        assertThat(request.method()).isEqualTo(HttpMethod.PUT);
        assertThat(request.ssl()).isTrue();
        assertThat(request.expectations()).containsOnly(ResponsePredicate.SC_OK);
    }

    @Test
    void create_a_token_request_and_verify(@Mock MultiMap headers) {
        given(originalRequest.headers()).willReturn(headers);
        given(options.createTokenOptions(headers)).willReturn(new RequestOptions());

        var request = sut.createTokenRequest(context);

        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.ssl()).isTrue();
        assertThat(request.expectations()).containsOnly(ResponsePredicate.SC_OK);
    }

    @Test
    void create_a_dataset_request_and_verify(@Mock MultiMap headers, @Mock TokenResponse tokenResponse) {
        given(originalRequest.headers()).willReturn(headers);
        given(options.createDatasetOptions(tokenResponse, headers)).willReturn(new RequestOptions());

        var request = sut.createDatasetRequest(context, tokenResponse);

        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.ssl()).isTrue();
        assertThat(request.expectations()).containsOnly(ResponsePredicate.SC_OK);
    }

}
