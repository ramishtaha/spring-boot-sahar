package com.ramishtaha.sahar.event;

import com.ramishtaha.sahar.domain.Location;

/**
 * A domain event: "the user changed the location prayer times are computed for" (step 22).
 *
 * <p>An event is just an immutable fact about something that already happened. The code that DID the thing
 * (saving the location) publishes this and moves on; it does not call - or even know about - whatever should
 * happen next. Anything interested subscribes with {@code @EventListener} /
 * {@code @TransactionalEventListener}. That inversion is the point: we can add reactions (recompute prayer
 * times, clear a cache, send a notification) without ever touching the code that saves the location.
 *
 * <p>Since Spring Framework 6 any object can be an event - no need to extend {@code ApplicationEvent} - so a
 * small {@code record} carrying just the new {@link Location} is the natural shape.
 */
public record LocationChangedEvent(Location location) {
}
