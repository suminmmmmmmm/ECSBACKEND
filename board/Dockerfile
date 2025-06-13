FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/board-0.0.1-SNAPSHOT-plain.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
