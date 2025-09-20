FROM openjdk:21-slim
WORKDIR /home/app
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /home/app/api-gateway.jar
CMD ["java", "-jar", "api-gateway.jar"]