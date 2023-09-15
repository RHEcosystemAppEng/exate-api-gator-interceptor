package exate.gator.interceptor.services;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;

/** Handler service provides handlers for handling the various responses. */
public interface HandlersService {
    /**
     * Used for handling responses from the underlying target service. Bypassing API-Gator occurs here.
     * @param context route context used to handle requests and send responses.
     * @return a lambda function handling the underlying target service responses.
     */
    Handler<HttpResponse<Buffer>> handlerTargetResponse(RoutingContext context);

    /**
     * Used for handling responses from API-Gator's TOKEN endpoint.
     * @param context route context used to handle requests and send responses.
     * @param targetResponse the response from the underlying target service is required for creating DATASET requests.
     * @return a lambda function handling API-Gator's TOKEN responses.
     */
    Handler<HttpResponse<Buffer>> handleTokenResponse(RoutingContext context, HttpResponse<Buffer> targetResponse);

    /**
     * Used for handling responses from API-Gator's DATASET endpoint.
     * @param context route context used to handle requests and send responses.
     * @param targetResponse the underlying target service original response is required for creating the final response.
     * @return a lambda function handling API-Gator's DATASET responses.
     */
    Handler<HttpResponse<Buffer>> handleDatasetResponse(RoutingContext context, HttpResponse<Buffer> targetResponse);

    Handler<Throwable> handleExceptions(String msg, int code, RoutingContext context);

    void handleErrors(HttpResponse<Buffer> response, RoutingContext context);
}
