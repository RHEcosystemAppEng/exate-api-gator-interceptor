package exate.gator.interceptor.configs;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration for the target server.
 * Properties here are used to determine the target server for proxying response from.
 */
@ConfigMapping(prefix = "target.server")
public interface TargetConfig {
    @NotNull
    Integer port();

    @NotBlank
    String host();

    @NotNull
    Boolean secure();
}
