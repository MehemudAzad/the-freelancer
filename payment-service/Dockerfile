FROM openjdk:21-slim
WORKDIR /home/app
COPY target/payment-service-0.0.1-SNAPSHOT.jar /home/app/payment-service.jar
CMD ["java", "-jar", "payment-service.jar"]