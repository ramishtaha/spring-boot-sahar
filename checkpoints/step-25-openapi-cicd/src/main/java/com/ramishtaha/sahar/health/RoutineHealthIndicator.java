package com.ramishtaha.sahar.health;

import com.ramishtaha.sahar.repo.MetaRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * A custom health check that appears under {@code /actuator/health} as the "routine" component.
 *
 * <p>Actuator already reports generic health (the datasource, disk space). A {@link HealthIndicator} lets
 * you add a domain-specific signal: here, "is the routine actually seeded and readable?" If the meta row is
 * missing the app is technically up but functionally broken, and this reports {@code DOWN} - exactly what a
 * load balancer or Kubernetes readiness probe should react to. Naming the bean {@code routine...} makes the
 * component key {@code routine}.
 */
@Component
public class RoutineHealthIndicator implements HealthIndicator {

    private final MetaRepository meta;

    public RoutineHealthIndicator(MetaRepository meta) {
        this.meta = meta;
    }

    @Override
    public Health health() {
        var found = meta.find();
        if (found.isEmpty()) {
            return Health.down().withDetail("reason", "app_meta is not seeded").build();
        }
        Health.Builder up = Health.up()
                .withDetail("title", found.get().title())
                .withDetail("month", found.get().month());
        meta.findLocation().ifPresent(loc -> up.withDetail("location", loc.placeName()));
        return up.build();
    }
}
