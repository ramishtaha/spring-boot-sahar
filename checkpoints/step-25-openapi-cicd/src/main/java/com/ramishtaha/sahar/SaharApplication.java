package com.ramishtaha.sahar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * The single entry point of the Sahar application.
 *
 * <p>{@code @SpringBootApplication} is three annotations rolled into one:
 * <ul>
 *   <li>{@code @SpringBootConfiguration} - marks this class as a source of bean definitions.</li>
 *   <li>{@code @EnableAutoConfiguration} - tells Spring Boot to configure beans by guessing from
 *       what is on the classpath (e.g. it sees Tomcat + Spring MVC and wires up a web server).</li>
 *   <li>{@code @ComponentScan} - scans this package and below for {@code @Component},
 *       {@code @Service}, {@code @RestController}, etc., and registers them as beans.</li>
 * </ul>
 *
 * <p>That last point is why the package matters: keep every other class under
 * {@code com.ramishtaha.sahar} (or a sub-package) so component scanning finds it.
 *
 * <p>{@code @ConfigurationPropertiesScan} (step 21) finds our {@code @ConfigurationProperties} records (e.g.
 * {@code GeocodingProperties}) and binds them, without a separate {@code @EnableConfigurationProperties}.
 * Caching is enabled in its own {@link com.ramishtaha.sahar.config.CacheConfig} rather than here, so the
 * {@code @WebMvcTest} slices don't drag in a {@code CacheManager} they have no use for.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SaharApplication {

    public static void main(String[] args) {
        // Boots the Spring "application context" (the container that holds all beans),
        // starts the embedded Tomcat server, and blocks until the app is shut down.
        SpringApplication.run(SaharApplication.class, args);
    }

}
