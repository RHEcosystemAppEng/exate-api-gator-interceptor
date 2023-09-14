package exate.gator.interceptor.factories;

import exate.gator.interceptor.TargetConfig;
import exate.gator.interceptor.api.ApiConfig;
import exate.gator.interceptor.api.DatasetPayload;
import exate.gator.interceptor.api.RequestHeaders;
import exate.gator.interceptor.api.TokenResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;

/** Use the functions in this static class for creating RequestOptions for the various target/api-gator requests. */
public class RequestOptionsFactory {
    private RequestOptionsFactory() {}

    /**
     * Create RequestOptions for sending requests to the underlying target service.
     * @param target TargetConfig for the target service.
     * @param originRequest Original request send to server, we use it for grabbing the original header and uri.
     * @return options for using with request to the target service.
     */
    public static RequestOptions createTargetOptions(TargetConfig target, HttpServerRequest originRequest) {
        return new RequestOptions()
            .setHost(target.host())
            .setPort(target.port())
            .setHeaders(originRequest.headers())
            .setURI(originRequest.uri());
    }

    /**
     * Create RequestOptions for sending requests to API-Gator's TOKEN endpoint.
     * @param config ApiConfig instance for creating the payload with its properties.
     * @param originHeaders headers from the original underlying target service request, used for overriding headers.
     * @return options for using with TOKEN requests to API-Gator.
     */
    public static RequestOptions createTokenOptions(ApiConfig config, MultiMap originHeaders) {
        return new RequestOptions()
            .setHost(config.host())
            .setPort(config.port())
            .setURI(config.tokenUri())
            .putHeader(
                RequestHeaders.X_Api_Key.toString(),
                originHeaders.contains(RequestHeaders.X_Api_Key.toString())
                    ? originHeaders.get(RequestHeaders.X_Api_Key.toString())
                    : config.apiKey())
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    }

    /**
     * Create RequestOptions for sending requests to API-Gator's DATASET endpoint.
     * @param config ApiConfig instance for creating the payload with its properties.
     * @param tokenResponse the response from the token endpoint is required for grabbing the token and type.
     * @param originHeaders headers from the original underlying target service request, used for overriding headers.
     * @return options for using with DATASET requests to API-Gator.
     */
    public static RequestOptions createDatasetOptions(ApiConfig config, TokenResponse tokenResponse, MultiMap originHeaders) {
        var opts = new RequestOptions()
            .setHost(config.host())
            .setPort(config.port())
            .setURI(config.datasetUri())
            .putHeader(RequestHeaders.X_Api_Key.toString(), config.apiKey())
            .putHeader(
                RequestHeaders.X_Resource_Token.toString(),
                String.format("%s %s", tokenResponse.token_type(), tokenResponse.access_token()))
            .putHeader(RequestHeaders.X_Data_Set_Type.toString(), DatasetPayload.DatasetType.JSON.toString())
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        // override api-gator headers with the ones from the original request exist.
        for (var header: RequestHeaders.values()) {
            var key = header.name();
            if (originHeaders.contains(key)) {
                opts.putHeader(key, originHeaders.get(key));
            }
        }

        return opts;
    }
}
