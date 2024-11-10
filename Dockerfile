FROM openjdk:21-bullseye AS base

RUN apt update && \
    apt -y upgrade && \
    apt -y install python3 python3-pip && \
    pip3 install --force-reinstall "Pillow==10.4.0"

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY ./src ./src
COPY ./pom.xml .
COPY ./resourcepac[k] ./resourcepack

RUN mvn package

FROM base AS runtime

WORKDIR /app
COPY --from=build /app/target/BoarBotJE.jar .
COPY --from=build /app/resourcepack ./resourcepack
RUN mkdir logs

ENTRYPOINT ["java", "-jar", "BoarBotJE.jar", "prod"]