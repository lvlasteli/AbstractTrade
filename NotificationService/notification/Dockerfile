
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy and install shared-events module first
COPY SharedEvents/shared-events/pom.xml /shared-events/pom.xml
COPY SharedEvents/shared-events/src /shared-events/src
RUN cd /shared-events && mvn clean install -DskipTests -B

# Now copy service files
COPY NotificationService/notification/pom.xml .
COPY NotificationService/notification/mvnw .
COPY NotificationService/notification/.mvn .mvn

RUN mvn dependency:go-offline -B

COPY NotificationService/notification/src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


