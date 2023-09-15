package exate.gator.interceptor.services;

import exate.gator.interceptor.content.DatasetResponse;
import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;

/** Requests creation service used for creating RequestOptions for the various requests. */
public interface RequestsService {
    /**
     * Create request for sending to the underlying target service.
     * @param context route context used to handle requests and send responses.
     * @return an HttpRequest of type Buffer, configured and ready to be sent to the underlying target service.
     */
    HttpRequest<Buffer> createTargetRequest(RoutingContext context);

    /**
     * Create request for sending to API-Gator's TOKEN endpoint.
     * @param context route context used to handle requests and send responses.
     * @return an HttpRequest of type TokenResponse, configured and ready to be sent to api gator service.
     */
    HttpRequest<TokenResponse> createTokenRequest(RoutingContext context);

    /**
     * Create request for sending to API-Gator's DATASET endpoint.
     * @param context route context used to handle requests and send responses.
     * @param tokenResponse a token response is required for fetching the token and token type.
     * @return an HttpRequest of type DatasetResponse, configured and ready to be sent to api gator service.
     */
    HttpRequest<DatasetResponse> createDatasetRequest(RoutingContext context, TokenResponse tokenResponse);
}
