# Stage 1: Build Application with Maven and JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and pre-fetch dependencies for Docker layer caching
COPY pom.xml .
RUN mvn dependency:resolve -B

# Copy application source code and compile jar (skipping unit tests for container build)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Ensure proper ownership
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Configure JVM for small container environments:
# -XX:MaxRAMPercentage=70: Limits heap memory to at most 70% of the container's available RAM, leaving headroom for JVM metaspace, thread stacks, and OS overhead to prevent cgroup OOM kills.
# -XX:+UseSerialGC: Enables the single-threaded Serial Garbage Collector, minimizing memory footprint and CPU overhead in constrained single-core containers.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseSerialGC", "-jar", "app.jar"]
