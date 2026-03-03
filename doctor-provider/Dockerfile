# ========================================
# Multi-Stage Build für doctor-provider
# ========================================

# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build

# UTF-8 Encoding setzen (wichtig für Umlaute in SQL-/Java-Dateien)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

WORKDIR /app

# Maven Wrapper und pom.xml kopieren (Dependency-Caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Maven Wrapper ausführbar machen
RUN chmod +x mvnw

# Dependencies herunterladen (wird gecacht, wenn pom.xml sich nicht ändert)
RUN ./mvnw dependency:go-offline -B || true

# Source Code kopieren
COPY src src

# Build ohne Tests und ohne Spotless (schneller)
RUN ./mvnw clean package -DskipTests -Dspotless.check.skip=true \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8 -B \
    && mv target/doctor-provider-0.0.1-SNAPSHOT.jar target/app.jar

# Stage 2: Run
# Java 25 - falls das Image nicht verfügbar ist, nutze: eclipse-temurin:24-jre
FROM eclipse-temurin:25-jre

# UTF-8 Encoding auch zur Laufzeit (für Flyway SQL-Migrationen mit Umlauten)
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /app

# JAR aus Build-Stage kopieren
COPY --from=build /app/target/app.jar app.jar

# Port freigeben
EXPOSE 8080

# Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]


