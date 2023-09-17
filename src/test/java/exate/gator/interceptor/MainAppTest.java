package exate.gator.interceptor;

import static org.mockito.BDDMockito.*;

import exate.gator.interceptor.content.*;
import exate.gator.interceptor.services.PayloadsService;
import exate.gator.interceptor.services.RequestsService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MainAppTest {
    @Mock PayloadsService payloads;
    @Mock RequestsService requests;
    @InjectMocks MainApp sut;
    @Mock RoutingContext context;
    @Mock HttpServerRequest originalRequest;
    @Mock HttpServerResponse response;
    @Mock HttpRequest<Buffer> targetRequest;
    @Mock RequestBody originalRequestBody;
    @Mock Buffer originalRequestBuffer;
    @Mock HttpResponse<Buffer> targetResponse;
    @Mock Buffer targetResponseBuffer;

    @BeforeEach
    void initialize() {
        when(originalRequest.absoluteURI()).thenReturn("fake-absolute-uri");
        when(context.request()).thenReturn(originalRequest);
        when(context.response()).thenReturn(response);

        when(requests.createTargetRequest(context)).thenReturn(targetRequest);
        when(originalRequestBody.buffer()).thenReturn(originalRequestBuffer);
        when(context.body()).thenReturn(originalRequestBody);
        when(targetRequest.sendBuffer(originalRequestBuffer)).thenReturn(Future.succeededFuture(targetResponse));
    }

    @Test
    void handling_requests_with_bypass_header_should_return_target_response() {
        given(targetResponse.bodyAsBuffer()).willReturn(targetResponseBuffer);
        given(response.end(targetResponseBuffer)).willReturn(Future.succeededFuture());
        given(originalRequest.getHeader(RequestHeaders.Api_Gator_Bypass.toString())).willReturn("true");

        sut.handle(context); // no assert is required, in strict mocking mode, no error is enough
    }

    @Test
    void handling_requests_without_bypass_header_should_return_dataset_response(
        @Mock HttpRequest<TokenResponse> tokenRequest,
        @Mock TokenPayload tokenPayload,
        @Mock HttpResponse<TokenResponse> tokenHttpResponse,
        @Mock TokenResponse tokenResponse,
        @Mock HttpRequest<DatasetResponse> datasetRequest,
        @Mock DatasetPayload datasetPayload,
        @Mock HttpResponse<DatasetResponse> datasetHttpResponse,
        @Mock DatasetResponse datasetResponse,
        @Mock HttpServerResponse contextResponse
    ) {
        given(requests.createTokenRequest(context)).willReturn(tokenRequest);
        given(payloads.createTokenPayload()).willReturn(tokenPayload);

        given(tokenHttpResponse.body()).willReturn(tokenResponse);
        given(tokenRequest.sendBuffer(Buffer.buffer(tokenPayload.toString())))
            .willReturn(Future.succeededFuture(tokenHttpResponse));

        var fakeOrigDataset = "{\"this_is\": \"a_fake_dataset\"}";
        given(targetResponse.bodyAsString()).willReturn(fakeOrigDataset);
        given(requests.createDatasetRequest(context, tokenResponse)).willReturn(datasetRequest);
        given(payloads.createDatasetPayload(fakeOrigDataset)).willReturn(datasetPayload);

        var fakeMaskedDataset = "{\"this_is\": \"a_fake_masked_dataset\"}";
        given(datasetResponse.dataSet()).willReturn(fakeMaskedDataset);
        given(datasetHttpResponse.body()).willReturn(datasetResponse);
        given(datasetRequest.sendJson(datasetPayload)).willReturn(Future.succeededFuture(datasetHttpResponse));

        var fakeHeaders = MultiMap.caseInsensitiveMultiMap();
        fakeHeaders.add("fake-header1", "fake-value1");
        fakeHeaders.add("fake-header2", "fake-value2");
        given(targetResponse.headers()).willReturn(fakeHeaders);

        given(contextResponse.putHeader("fake-header1", "fake-value1")).willReturn(contextResponse);
        given(contextResponse.putHeader("fake-header2", "fake-value2")).willReturn(contextResponse);
        given(contextResponse.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fakeMaskedDataset.length()))).willReturn(contextResponse);

        given(contextResponse.end(Buffer.buffer(fakeMaskedDataset))).willReturn(Future.succeededFuture());
        given(context.response()).willReturn(contextResponse);

        sut.handle(context);  // no assert is required, in strict mocking mode, no error is enough
    }

}
