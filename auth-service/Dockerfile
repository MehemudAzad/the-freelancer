FROM openjdk:21-slim
WORKDIR /home/app
COPY target/auth-service-0.0.1-SNAPSHOT.jar /home/app/auth-service.jar
CMD ["java", "-jar", "auth-service.jar"]