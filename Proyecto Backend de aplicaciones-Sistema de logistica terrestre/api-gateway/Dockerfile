# ============================================================
# Dockerfile Multi-Stage para API Gateway
# ============================================================

# ==================== STAGE 1: BUILD ====================
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar pom.xml y descargar dependencias (cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ==================== STAGE 2: RUNTIME ====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR desde stage de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
