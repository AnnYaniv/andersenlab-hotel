FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/*.jar kafka-consumer-1.0.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=40"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/kafka-consumer-1.0.jar"]