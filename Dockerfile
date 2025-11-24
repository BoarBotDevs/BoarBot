FROM openjdk:26-ea-21-bookworm AS base

RUN apt update && \
    apt -y upgrade && \
    apt -y install python3 python3-pip && \
    pip3 install --break-system-packages --force-reinstall "Pillow==10.4.0"

ENV _JAVA_OPTIONS="-Xmx4g"

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY ./src ./src
COPY ./pom.xml .

RUN mvn package

FROM base AS runtime

WORKDIR /app
COPY --from=build /app/target/BoarBotJE.jar .
COPY ./resourcepac[k] ./resourcepack
COPY ./database/scripts ./database/scripts
RUN mkdir logs

ENTRYPOINT ["java", "-jar", "BoarBotJE.jar", "prod"]