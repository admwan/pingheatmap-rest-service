FROM openjdk:23-jdk-slim
WORKDIR /app
COPY pingerdaemon-rabbitmq-client-0.0.1-SNAPSHOT.jar /app/pingerdaemon-rabbitmq-client-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/app/pingerdaemon-rabbitmq-client-0.0.1-SNAPSHOT.jar", "THORFW"]
