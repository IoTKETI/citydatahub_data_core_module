FROM eclipse-temurin:11
WORKDIR /
ADD target/data-service-broker-2.0.3.jar app.jar
ENTRYPOINT [ "java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Djava.net.preferIPv4Stack=true", \
    "-Dspring.profiles.active=docker", \
    "-jar", \
    "app.jar"]
