FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/order-service-2.0.jar OrderService.jar
ENTRYPOINT ["java", "-jar", "OrderService.jar"]