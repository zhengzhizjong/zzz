FROM openjdk:11-jre-slim
WORKDIR /app
COPY zjt-app/target/zjt-app-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "-Dspring.profiles.active=prod", "app.jar"]
