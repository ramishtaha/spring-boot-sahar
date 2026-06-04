package com.ramishtaha.sahar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Turns on Spring's cache abstraction (step 21). {@code @EnableCaching} registers the aspect that makes
 * {@code @Cacheable} methods actually consult a {@link org.springframework.cache.CacheManager} - here the
 * Caffeine manager auto-configured from {@code spring.cache.*} in {@code application.properties}.
 *
 * <p>Why a separate class instead of putting {@code @EnableCaching} on {@code SaharApplication}? A
 * {@code @WebMvcTest} slice loads the application class to find its configuration, but deliberately leaves
 * out the cache auto-configuration - so an app-level {@code @EnableCaching} would demand a
 * {@code CacheManager} the slice never creates, and the slice would fail to start. Keeping caching in its
 * own {@code @Configuration} (which web slices don't load) avoids that coupling: a small but real lesson in
 * how Boot's test slices shape where you put cross-cutting config.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
