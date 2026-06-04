package com.ramishtaha.sahar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The simplest possible Spring Boot test: it starts the whole application context
 * and fails if any bean cannot be created or wired. If this passes, your app boots.
 *
 * <p>{@code @SpringBootTest} loads the full context exactly as {@code main} would,
 * but inside JUnit. The empty {@code contextLoads} test body is intentional - the
 * act of starting the context IS the assertion.
 */
@SpringBootTest
class SaharApplicationTests {

    @Test
    void contextLoads() {
    }

}
