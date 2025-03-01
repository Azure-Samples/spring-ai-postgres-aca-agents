# Build stage
FROM mcr.microsoft.com/openjdk/jdk:21-mariner AS build
WORKDIR /app
COPY mvnw* .
COPY .mvn ./.mvn
COPY pom.xml .
COPY src ./src
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline package -DskipTests

# Runtime stage
FROM mcr.microsoft.com/openjdk/jdk:21-mariner
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "app.jar"]
