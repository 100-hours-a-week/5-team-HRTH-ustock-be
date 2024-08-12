FROM gradle:8.2-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./

RUN gradle dependencies --no-daemon

COPY . /app

RUN gradle clean build --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/ustock.jar

EXPOSE 8080

CMD ["java", "-jar", "ustock.jar"]