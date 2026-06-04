package com.ramishtaha.sahar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Type-safe configuration for the geocoder, bound from {@code sahar.geocoding.*} (step 21).
 *
 * <p>Instead of scattering {@code @Value("${...}")} strings through the code, we bind a whole group of
 * related settings into one immutable record. Spring validates the types at startup, IntelliJ autocompletes
 * the keys in {@code application.properties} (thanks to the configuration-processor on the classpath), and
 * the service depends on a small, documented object rather than raw strings.
 *
 * <p>Each {@link DefaultValue} means the app works out of the box with no {@code sahar.geocoding.*} set;
 * any key you do add in {@code application.properties} (or an env var, or a {@code --flag}) overrides it.
 */
@ConfigurationProperties(prefix = "sahar.geocoding")
public record GeocodingProperties(
        @DefaultValue("https://nominatim.openstreetmap.org") String baseUrl,
        @DefaultValue("Sahar/1.0 (https://github.com/ramishtaha/spring-boot-sahar)") String userAgent,
        @DefaultValue("3000") int connectTimeoutMs,
        @DefaultValue("5000") int readTimeoutMs,
        @DefaultValue("2") int maxRetries) {
}
