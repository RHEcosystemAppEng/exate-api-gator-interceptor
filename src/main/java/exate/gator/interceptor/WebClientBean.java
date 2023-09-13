package exate.gator.interceptor;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

public class WebClientBean {
    @Produces
    @ApplicationScoped
    public WebClient createClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}
