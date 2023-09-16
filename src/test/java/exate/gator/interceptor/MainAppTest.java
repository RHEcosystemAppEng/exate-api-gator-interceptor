package exate.gator.interceptor;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.BDDMockito.*;

import exate.gator.interceptor.content.RequestHeaders;
import exate.gator.interceptor.services.PayloadsService;
import exate.gator.interceptor.services.RequestsService;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
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
        when(targetResponse.bodyAsBuffer()).thenReturn(targetResponseBuffer);
        when(response.end(targetResponseBuffer)).thenReturn(Future.succeededFuture());
    }

    @Test
    void handling_requests_with_bypass_header_should_return_target_response() {
        given(originalRequest.getHeader(RequestHeaders.Api_Gator_Bypass.toString())).willReturn("true");
        sut.handle(context); // no assert is required, in strict mocking mode, no error is enough
    }

}
