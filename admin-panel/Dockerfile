# ============================================================================
# DOCKERFILE PARA ADMIN-PANEL (VAADIN + SPRING BOOT)
# ============================================================================
# Multi-stage build: compilar con Maven, ejecutar con JRE ligero

FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="IFit System"
LABEL description="Admin Panel - Vaadin UI para IFit"

RUN apk add --no-cache curl netcat-openbsd

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

ENV JAVA_OPTS="-Xmx512m -Xms256m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
