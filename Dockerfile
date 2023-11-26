FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY spring-interface/target/*.jar spring-interface-1.0.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/spring-interface-1.0.jar"]