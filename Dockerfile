# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src src

RUN ./mvnw \
    --batch-mode \
    --no-transfer-progress \
    clean package \
    -DskipTests


FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system spring \
    && useradd \
        --system \
        --gid spring \
        --no-create-home \
        spring

COPY --from=build \
    --chown=spring:spring \
    /workspace/target/*.jar \
    app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]