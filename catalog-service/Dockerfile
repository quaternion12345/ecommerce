FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/catalog-service-2.0.jar CatalogService.jar
ENTRYPOINT ["java", "-jar", "CatalogService.jar"]