FROM maven:3.9.8-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode --no-transfer-progress
COPY src ./src
RUN mvn clean package -DskipTests --batch-mode --no-transfer-progress

FROM eclipse-temurin:22-jre-jammy
ARG JAR_FILE=target/SpringToDo-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} app.jar
RUN useradd -m appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]