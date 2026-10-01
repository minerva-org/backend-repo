FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace

COPY . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:26-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]