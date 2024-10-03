FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY ./build/libs/ustock-0.0.1-SNAPSHOT.jar /app/ustock.jar

RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

CMD ["java", "-jar", "ustock.jar"]
