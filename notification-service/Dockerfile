FROM openjdk:21-slim
WORKDIR /home/app
COPY target/notification-service-0.0.1-SNAPSHOT.jar /home/app/notification-service.jar
CMD ["java", "-jar", "notification-service.jar"]