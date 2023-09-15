package exate.gator.interceptor;

import exate.gator.interceptor.configs.TargetConfig;
import exate.gator.interceptor.services.HandlersService;
import exate.gator.interceptor.services.OptionsService;
import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Main application entrypoint, interception occurs here. */
@ApplicationScoped
public class InterceptorApp {
    private final WebClient client;
    private final OptionsService options;
    private final HandlersService handlers;
    private final TargetConfig target;

    @Inject
    public InterceptorApp(WebClient client, OptionsService options, HandlersService handlers, TargetConfig target) {
        this.client = client;
        this.options = options;
        this.handlers = handlers;
        this.target = target;
    }

    /**
     * Main entrypoint for the interceptor application, all requests stars here.
     * @param context injected by Quarkus, used to handle requests and send responses.
     */
    @Route(regex = ".*")
    public void handle(RoutingContext context) {
        Log.debugf("got new request, %s", context.request().absoluteURI());
        // create target request options using origin request and target host and port
        var targetRequestOpts = this.options.createTargetOptions(context.request());
        // wrap target request with our own
        var targetRequest = this.client.request(context.request().method(), targetRequestOpts);
        Log.info("proxying request to target");
        targetRequest
            .ssl(this.target.secure())
            .sendBuffer(context.body().buffer())
            .onSuccess(handlers.handlerTargetResponse(context))
            .onFailure(handlers.handleExceptions("failed proxying request to target", 500, context));
    }
}
