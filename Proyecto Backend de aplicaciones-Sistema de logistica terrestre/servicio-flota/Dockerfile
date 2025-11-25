# =====================================================
# DOCKERFILE - SERVICIO FLOTA
# =====================================================
# Multi-stage build para optimizar tamaño de imagen
# =====================================================

# ============================================================
# STAGE 1: Build
# ============================================================
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación (skip tests para acelerar build)
RUN mvn clean package -DskipTests

# ============================================================
# STAGE 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Crear directorio de logs con permisos correctos
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

# Copiar JAR desde stage de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8081

# Variables de entorno (pueden ser sobreescritas en docker-compose)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
