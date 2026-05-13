# ============================================================
# Dockerfile — FermeDirecte Spring Boot
# Multi-stage build : compile avec Maven, run avec JRE léger
# ============================================================

# --- Stage 1 : Build ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier pom.xml et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2 : Run ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le JAR compilé
COPY --from=build /app/target/FermeDirecte-2.0.0.jar app.jar

# Railway injecte PORT automatiquement
EXPOSE 8080

# sh -c permet à $PORT d'être résolu depuis l'environnement Railway
CMD ["sh", "-c", "java -Xms64m -Xmx256m -XX:+UseSerialGC -Dspring.profiles.active=railway -Dserver.port=${PORT:-8080} -jar app.jar"]
