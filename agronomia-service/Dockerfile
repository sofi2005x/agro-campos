# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 1) Instalar bda-commons en el repositorio Maven de esta etapa
COPY bda-commons/pom.xml bda-commons/pom.xml
COPY bda-commons/src bda-commons/src
RUN mvn -f bda-commons/pom.xml install -DskipTests -B

# 2) Compilar el servicio (ahora encuentra bda-commons)
COPY agronomia-service/pom.xml agronomia-service/pom.xml
RUN mvn -f agronomia-service/pom.xml dependency:go-offline -B
COPY agronomia-service/src agronomia-service/src
RUN mvn -f agronomia-service/pom.xml package -DskipTests -B

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# curl para el healthcheck del compose (se instala como root, antes de cambiar de usuario)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuario sin privilegios
RUN useradd -r -u 1001 appuser

COPY --from=build --chown=appuser:appuser /build/agronomia-service/target/*.jar app.jar

USER appuser

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]