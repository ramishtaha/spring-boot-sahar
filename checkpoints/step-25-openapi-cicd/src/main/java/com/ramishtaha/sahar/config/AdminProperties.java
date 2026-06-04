package com.ramishtaha.sahar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The single admin credential, bound from {@code sahar.admin.*} (step 24).
 *
 * <p>Sahar has exactly one writer - you - so a single in-memory user is the honest amount of auth here, not a
 * user table. The defaults make it work out of the box for local development; in any real deployment you
 * override {@code sahar.admin.password} with an environment variable (e.g. {@code SAHAR_ADMIN_PASSWORD}) and
 * never commit a real secret. Relaxed config binding maps that env var to this property automatically.
 */
@ConfigurationProperties(prefix = "sahar.admin")
public record AdminProperties(
        @DefaultValue("admin") String username,
        @DefaultValue("sahar") String password) {
}
