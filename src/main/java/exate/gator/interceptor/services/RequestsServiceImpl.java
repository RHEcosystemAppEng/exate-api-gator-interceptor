package exate.gator.interceptor.services;

import exate.gator.interceptor.configs.TargetConfig;
import exate.gator.interceptor.content.DatasetResponse;
import exate.gator.interceptor.content.TokenResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RequestsServiceImpl implements RequestsService {
    private final WebClient client;
    private final OptionsService options;
    private final TargetConfig target;

    @Inject
    public  RequestsServiceImpl(WebClient client, OptionsService options, TargetConfig target) {
        this.client = client;
        this.options = options;
        this.target = target;
    }

    public HttpRequest<Buffer> createTargetRequest(RoutingContext context) {
        var targetRequestOpts = this.options.createTargetOptions(context.request());
        return this.client.request(context.request().method(), targetRequestOpts)
            .ssl(this.target.secure())
            .expect(ResponsePredicate.SC_OK);
    }

    public HttpRequest<TokenResponse> createTokenRequest(RoutingContext context) {
        var tokenReqOpts = this.options.createTokenOptions(context.request().headers());
        return this.client.request(HttpMethod.POST, tokenReqOpts)
            .ssl(true)
            .expect(ResponsePredicate.SC_OK)
            .as(BodyCodec.json(TokenResponse.class));
    }

    public HttpRequest<DatasetResponse> createDatasetRequest(RoutingContext context, TokenResponse tokenResponse) {
        var datasetReqOpts = this.options.createDatasetOptions(tokenResponse, context.request().headers());
        return this.client.request(HttpMethod.POST, datasetReqOpts)
            .ssl(true)
            .expect(ResponsePredicate.SC_OK)
            .as(BodyCodec.json(DatasetResponse.class));
    }
}
