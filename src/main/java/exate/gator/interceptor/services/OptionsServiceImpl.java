package exate.gator.interceptor.services;

import exate.gator.interceptor.configs.GatorConfig;
import exate.gator.interceptor.configs.TargetConfig;
import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.RequestHeaders;
import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OptionsServiceImpl implements OptionsService {
    private final GatorConfig gator;
    private final TargetConfig target;

    @Inject
    public OptionsServiceImpl(GatorConfig gator, TargetConfig target) {
        this.gator = gator;
        this.target = target;
    }

    public RequestOptions createTargetOptions(HttpServerRequest originRequest) {
        return new RequestOptions()
            .setHost(this.target.host())
            .setPort(this.target.port())
            .setHeaders(originRequest.headers())
            .setURI(originRequest.uri());
    }

    public RequestOptions createTokenOptions(MultiMap originHeaders) {
        return new RequestOptions()
            .setHost(this.gator.host())
            .setPort(this.gator.port())
            .setURI(this.gator.tokenUri())
            .putHeader(
                RequestHeaders.X_Api_Key.toString(),
                originHeaders.contains(RequestHeaders.X_Api_Key.toString())
                    ? originHeaders.get(RequestHeaders.X_Api_Key.toString())
                    : this.gator.apiKey())
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    }

    public RequestOptions createDatasetOptions(TokenResponse tokenResponse, MultiMap originHeaders) {
        var opts = new RequestOptions()
            .setHost(this.gator.host())
            .setPort(this.gator.port())
            .setURI(this.gator.datasetUri())
            .putHeader(RequestHeaders.X_Api_Key.toString(), this.gator.apiKey())
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
