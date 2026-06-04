# syntax=docker/dockerfile:1
#
# Sahar - multi-stage Dockerfile.
#
# An IMAGE is a read-only template (this file, baked). A CONTAINER is a running instance of an image.
# A multi-stage build uses two images: a big one to BUILD, and a small one to RUN. Only the run stage
# ships, so the compiler and Maven (hundreds of MB) never make it into the image you deploy.

# ---- Stage 1: build ---------------------------------------------------------
# Full JDK + Maven. Used only to compile and package; thrown away after.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy the pom first and resolve dependencies on its own layer. Because Docker caches layers and only
# rebuilds from the first changed line down, editing Java source does NOT re-download dependencies.
COPY pom.xml .

# Build the executable jar. The --mount cache keeps Maven's ~/.m2 download cache between builds, so
# repeat builds are fast; it is a build-time cache and is NOT part of the final image.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests clean package

# ---- Stage 2: run -----------------------------------------------------------
# A slim JRE - no compiler, no Maven. The shipped image is a fraction of the build image's size.
FROM eclipse-temurin:25-jre AS run
WORKDIR /app

# Create a non-root user. If the container is ever compromised, the attacker is not root.
RUN useradd --system --no-create-home --uid 10001 sahar

# Copy ONLY the built jar out of the build stage. Nothing else from stage 1 comes along.
COPY --from=build /app/target/sahar-0.0.1-SNAPSHOT.jar app.jar

# Make /app (the jar AND the ./data folder the default H2 file needs) writable by our user,
# then drop to it. Without this, the non-root user could not create the H2 database file.
RUN mkdir -p /app/data && chown -R sahar:sahar /app
USER sahar

# Document the port the app listens on (this does not publish it - `-p` at run time does).
EXPOSE 8080

# All configuration comes from the environment (12-factor), e.g.:
#   -e SPRING_PROFILES_ACTIVE=postgres
#   -e SAHAR_DB_URL=jdbc:postgresql://db:5432/sahar -e SAHAR_DB_USERNAME=sahar -e SAHAR_DB_PASSWORD=...
# With no env set, the app uses its default H2 file database inside the container.
ENTRYPOINT ["java", "-jar", "app.jar"]
