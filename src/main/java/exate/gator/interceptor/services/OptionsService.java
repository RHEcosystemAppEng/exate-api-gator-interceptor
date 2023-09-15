package exate.gator.interceptor.services;

import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;

/** Options creation service used for creating RequestOptions for the various requests. */
public interface OptionsService {
    /**
     * Create RequestOptions for sending requests to the underlying target service.
     * @param originRequest Original request send to server, we use it for grabbing the original header and uri.
     * @return options for using with request to the target service.
     */
    RequestOptions createTargetOptions(HttpServerRequest originRequest);

    /**
     * Create RequestOptions for sending requests to API-Gator's TOKEN endpoint.
     * @param originHeaders headers from the original underlying target service request, used for overriding headers.
     * @return options for using with TOKEN requests to API-Gator.
     */
    RequestOptions createTokenOptions(MultiMap originHeaders);

    /**
     * Create RequestOptions for sending requests to API-Gator's DATASET endpoint.
     * @param tokenResponse the response from the token endpoint is required for grabbing the token and type.
     * @param originHeaders headers from the original underlying target service request, used for overriding headers.
     * @return options for using with DATASET requests to API-Gator.
     */
    RequestOptions createDatasetOptions(TokenResponse tokenResponse, MultiMap originHeaders);
}
