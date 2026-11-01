# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy the Maven wrapper and pom first so dependencies are cached
# and only re-downloaded when pom.xml changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Now copy the source and build. Tests are skipped here because they use
# Testcontainers, which needs Docker access that isn't available during a build.
COPY src src
RUN ./mvnw -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as a non-root user, and give it a writable uploads folder
RUN useradd --system --create-home appuser \
    && mkdir -p /app/uploads \
    && chown appuser /app/uploads
USER appuser

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]