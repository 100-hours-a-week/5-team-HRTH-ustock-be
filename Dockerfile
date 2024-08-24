FROM amazoncorretto:17-alpine-jdk

WORKDIR /app
# 각자의 프로젝트 파일에 맞게 변경하십셔
COPY ./build/libs/ustock-0.0.1-SNAPSHOT.jar /app/ustock.jar

RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

CMD ["java", "-jar", "ustock.jar"]