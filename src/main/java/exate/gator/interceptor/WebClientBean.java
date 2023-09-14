package exate.gator.interceptor;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class WebClientBean {
    /**
     * The webclient bean is used for sending the underlying request to the target service.
     * @param vertx Eclipse vert.x, injected by Quarkus is used for creating reactive applications.
     * @return the WebClient instantiated application-scoped singleton.
     */
    @Produces
    @ApplicationScoped
    public WebClient createClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}
