package exate.gator.interceptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exate.gator.interceptor.factories.PayloadFactory;
import exate.gator.interceptor.factories.RequestOptionsFactory;
import exate.gator.interceptor.api.ApiConfig;
import exate.gator.interceptor.api.DatasetResponse;
import exate.gator.interceptor.api.RequestHeaders;
import exate.gator.interceptor.api.TokenResponse;
import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Route;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;

/** Main application entrypoint, interception occurs here. */
@ApplicationScoped
public class InterceptorApp {
    private final WebClient client;
    private final ObjectMapper mapper;
    private final TargetConfig target;
    private final ApiConfig config;

    @Inject
    public InterceptorApp(WebClient client, ObjectMapper mapper, TargetConfig target, ApiConfig config) {
        this.client = client;
        this.mapper = mapper;
        this.target = target;
        this.config = config;
    }

    @PostConstruct
    void initialize() {
        // this allows us to set null to values for non-mandatory fields.
        // null values will not be included for serializing/deserializing.
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * Main entrypoint for the interceptor application, all requests stars here.
     * @param context injected by Quarkus, used to handle requests and send responses.
     */
    @Route(regex = ".*")
    public void handle(RoutingContext context) {
        Log.debugf("got new request, %s", context.request().absoluteURI());
        // create target request options using origin request and target host and port
        var targetRequestOpts = RequestOptionsFactory.createTargetOptions(target, context.request());
        // wrap target request with our own
        var targetRequest = this.client.request(context.request().method(), targetRequestOpts);
        Log.info("proxying request to target");
        targetRequest
            .ssl(this.target.secure())
            .sendBuffer(context.body().buffer())
            .onSuccess(handlerTargetResponse(context))
            .onFailure(handleExceptions("failed proxying request to target", 500, context));
    }

    /**
     * Used for handling responses from the underlying target service. Bypassing API-Gator occurs here.
     * @param context route context used to handle requests and send responses.
     * @return a lambda function handling the underlying target service responses.
     */
    private Handler<HttpResponse<Buffer>> handlerTargetResponse(RoutingContext context) {
        return targetResponse -> {
            Log.info("proxying request to target successful");
            if (targetResponse.statusCode() != 200) {
                handleErrors(targetResponse, context);
                return;
            }
            var bypass = context.request().getHeader(RequestHeaders.Api_Gator_Bypass.toString());
            if (Objects.nonNull(bypass) && bypass.equals("true")) {
                Log.info("bypassing gator and returning target response");
                // end with the original target service response
                context.response().end(targetResponse.bodyAsBuffer());;
                Log.info("bypassing gator and returning target response successful");

                return;
            }
            // create token request options
            var tokenReqOpts = RequestOptionsFactory.createTokenOptions(this.config, context.request().headers());
            // build token request payload
            var tokenReqPayload = PayloadFactory.createTokenPayload(this.config);
            // create token request
            var tokenRequest = client.request(HttpMethod.POST, tokenReqOpts);
            Log.info("sending token request to gator");
            tokenRequest
                .ssl(true)
                .sendBuffer(Buffer.buffer(tokenReqPayload.toString()))
                .onSuccess(handleTokenResponse(context, targetResponse))
                .onFailure(handleExceptions("failed fetching token from gator", 500, context));

        };
    }

    /**
     * Used for handling responses from API-Gator's TOKEN endpoint.
     * @param context route context used to handle requests and send responses.
     * @param targetResponse the response from the underlying target service is required for creating DATASET requests.
     * @return a lambda function handling API-Gator's TOKEN responses.
     */
    private Handler<HttpResponse<Buffer>> handleTokenResponse(RoutingContext context, HttpResponse<Buffer> targetResponse) {
        return tokenResponse -> {
            Log.info("sending token request to gator successful");
            if (tokenResponse.statusCode() != 200) {
                handleErrors(tokenResponse, context);
                return;
            }
            TokenResponse parsedTokenResp = null;
            try {
                parsedTokenResp = mapper.readValue(tokenResponse.bodyAsString(), TokenResponse.class);
            } catch (JsonProcessingException e) {
                handleExceptions("failed parsing gator token response", 400, context).handle(e);
                return;
            }
            // create dataset request options
            var datasetReqOpts = RequestOptionsFactory.createDatasetOptions(
                this.config, parsedTokenResp, context.request().headers());
            // build dataset request payload
            var datasetReqPayload = PayloadFactory.createDatasetPayload(this.config, targetResponse.bodyAsString());
            // create dataset request
            var datasetRequest = this.client.request(HttpMethod.POST, datasetReqOpts);
            Log.info("sending dataset request to gator");
            String datasetReqStr = null;
            try {
                datasetReqStr = mapper.writeValueAsString(datasetReqPayload);
            } catch (JsonProcessingException e) {
                handleExceptions("failed parsing gator request payload", 400, context).handle(e);
                return;
            }
            datasetRequest
                .ssl(true)
                .sendBuffer(Buffer.buffer(datasetReqStr))
                .onSuccess(handleDatasetResponse(context, targetResponse))
                .onFailure(handleExceptions("failed intercepting response with gator", 500, context));
        };
    }

    /**
     * Used for handling responses from API-Gator's DATASET endpoint.
     * @param context route context used to handle requests and send responses.
     * @param targetResponse the underlying target service original response is required for creating the final response.
     * @return a lambda function handling API-Gator's DATASET responses.
     */
    private Handler<HttpResponse<Buffer>> handleDatasetResponse(RoutingContext context, HttpResponse<Buffer> targetResponse) {
        return datasetResponse -> {
            Log.info("sending dataset request to gator successful");
            if (datasetResponse.statusCode() != 200) {
                handleErrors(datasetResponse, context);
                return;
            }
            DatasetResponse parsedDatasetResp = null;
            try {
                parsedDatasetResp = mapper.readValue(datasetResponse.bodyAsString(), DatasetResponse.class);
            } catch (JsonProcessingException e) {
                handleExceptions("failed parsing gator dataset response", 400, context).handle(e);
                return;
            }
            Log.info("returning gator dataset as response");
            // include target response headers in gator'ed response
            targetResponse.headers().forEach((k, v) -> context.response().putHeader(k, v));
            context.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(parsedDatasetResp.dataSet().length()));
            // end with the gator'ed response
            context.response().end(Buffer.buffer(parsedDatasetResp.dataSet()));
            Log.info("returning gator dataset as response successful");
        };
    }

    private void handleErrors(HttpResponse<Buffer> response, RoutingContext context) {
        Log.error(response.statusMessage());
        Log.debug(response.body());
        context.response().setStatusCode(response.statusCode());
        context.response().end(response.statusMessage());
    }

    private Handler<Throwable> handleExceptions(String msg, int code, RoutingContext context) {
        return t -> {
            Log.error(msg, t);
            context.response().setStatusCode(code);
            context.response().end(msg);
        };
    }
}
