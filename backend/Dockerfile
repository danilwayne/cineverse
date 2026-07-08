FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# -XX:+UseSerialGC: container pequeno (free tier ~512MB); o GC serial gasta menos memória/CPU.
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-jar", "app.jar"]
