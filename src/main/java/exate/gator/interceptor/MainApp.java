package exate.gator.interceptor;

import exate.gator.interceptor.content.RequestHeaders;
import exate.gator.interceptor.services.PayloadsService;
import exate.gator.interceptor.services.RequestsService;
import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Route;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Objects;

/** Main application entrypoint, interception occurs here. */
@ApplicationScoped
public class MainApp {
    private final PayloadsService payloads;
    private final RequestsService requests;

    @Inject
    public MainApp(PayloadsService payloads, RequestsService requests) {
        this.payloads = payloads;
        this.requests = requests;
    }

    /**
     * Main entrypoint for the interceptor application, all requests starts here.
     * @param context injected by Quarkus, used for handling requests and responses.
     */
    @Route(regex = ".*")
    public void handle(RoutingContext context) {
        Log.debugf("got new request, %s", context.request().absoluteURI());
        var targetRequest = requests.createTargetRequest(context);

        Log.info("proxying request to target");
        targetRequest.sendBuffer(context.body().buffer())
            .onSuccess(targetResponse -> {
                var bypass = context.request().getHeader(RequestHeaders.Api_Gator_Bypass.toString());
                if (Objects.nonNull(bypass) && bypass.equals("true")) {
                    Log.info("bypassing gator and returning target response");
                    context.response().end(targetResponse.bodyAsBuffer());;
                    return;
                }

                var tokenRequest = requests.createTokenRequest(context);
                var tokenReqPayload = this.payloads.createTokenPayload();

                Log.info("sending token request to gator");
                tokenRequest.sendBuffer(Buffer.buffer(tokenReqPayload.toString()))
                    .onSuccess(tokenResponse -> {
                        var datasetRequest = requests.createDatasetRequest(context, tokenResponse.body());
                        var datasetReqPayload = this.payloads.createDatasetPayload(targetResponse.bodyAsString());

                        Log.info("sending dataset request to gator");
                        datasetRequest.sendJson(datasetReqPayload)
                            .onSuccess(datasetResponse -> {
                                var newDataset = datasetResponse.body().dataSet();
                                // grab headers from target response and set in end response
                                targetResponse.headers().forEach((k, v) -> context.response().putHeader(k, v));
                                context.response().putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(newDataset.length()));

                                Log.info("returning gator dataset as response");
                                context.response().end(Buffer.buffer(newDataset));
                            })
                            .onFailure(handleExceptions("failed creating new dataset with gator", context));
                    })
                    .onFailure(handleExceptions("failed fetching token from gator", context));
            })
            .onFailure(handleExceptions("failed proxying request to target", context));
    }

    private Handler<Throwable> handleExceptions(String msg, RoutingContext context) {
        return t -> {
            Log.error(msg, t);
            context.response().setStatusCode(500);
            context.response().end(msg);
        };
    }
}
