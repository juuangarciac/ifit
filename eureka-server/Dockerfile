# ============================================================================
# DOCKERFILE PARA MICROSERVICIOS SPRING BOOT - SISTEMA IFIT
# ============================================================================
# 
# Este Dockerfile usa multi-stage build:
# - Stage 1: Compilar con Maven
# - Stage 2: Ejecutar con JRE ligero
#
# Uso:
#   docker build -t nombre-microservicio .
#
# ============================================================================

# ============================================================================
# STAGE 1: BUILD - Compilación con Maven
# ============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Establecer directorio de trabajo
WORKDIR /build

# Copiar archivos de Maven para aprovechar caché de Docker
# (las dependencias no cambian tan frecuentemente como el código)
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar proyecto (saltar tests para acelerar build)
# Para ejecutar tests: cambiar -DskipTests por -Dmaven.test.skip=false
RUN mvn clean package -DskipTests -B

# ============================================================================
# STAGE 2: RUNTIME - Imagen ligera solo con JRE y JAR
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="IFit System"
LABEL description="Microservicio Spring Boot para IFit"

# Instalar herramientas útiles para debugging
RUN apk add --no-cache curl netcat-openbsd

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=build /build/target/*.jar app.jar

# Dar permisos al usuario spring
RUN chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring

# Variables de entorno para optimizar Java en contenedores
ENV JAVA_OPTS="-Xmx512m -Xms256m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Health check (opcional, ajustar path según tu app)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1

# Exponer puerto (cambiar según el microservicio)
# Esto es documentación, el puerto real se configura en docker-compose.yml
EXPOSE 8080

# Comando de inicio
# Usa shell para evaluar variables de entorno
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
