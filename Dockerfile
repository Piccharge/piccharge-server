FROM amazoncorretto:17-alpine

WORKDIR /app

COPY build/libs/piccharge-server-0.0.1-SNAPSHOT.jar /app/piccharge-server.jar
COPY config/ /app/

EXPOSE 8080

# 컨테이너 시작 시 실행할 명령
CMD ["java", "-jar", "/app/piccharge-server.jar"]
