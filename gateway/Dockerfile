# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar solo el pom primero para cachear las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código y compilar
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# curl para el healthcheck del compose (se instala como root, antes de cambiar de usuario)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuario sin privilegios
RUN useradd -r -u 1001 appuser

COPY --from=build --chown=appuser:appuser /build/target/*.jar app.jar

USER appuser

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]