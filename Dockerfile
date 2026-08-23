FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package

FROM quay.io/keycloak/keycloak:26.3.3
COPY --from=build /workspace/target/keycloak-fcaptcha-0.1.0.jar /opt/keycloak/providers/keycloak-fcaptcha.jar
RUN /opt/keycloak/bin/kc.sh build
