FROM maven:3.8.7 as builder

WORKDIR /app

COPY pom.xml .

COPY src src

RUN mvn clean package

FROM eclipse-temurin:17-jre-alpine

COPY --from=builder /app/target/*.jar /app/spring-app.jar

ENTRYPOINT ["java","-jar","/app/spring-app.jar"]
