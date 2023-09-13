package exate.gator.interceptor;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigMapping(prefix = "target.server")
public interface TargetConfig {
    @NotNull
    Integer port();

    @NotBlank
    String host();

    @NotNull
    Boolean secure();
}
