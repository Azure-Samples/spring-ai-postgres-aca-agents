# Build stage
FROM mcr.microsoft.com/openjdk/jdk:21-mariner AS build
WORKDIR /app
COPY mvnw* /app/
COPY .mvn /app/.mvn
COPY pom.xml /app
COPY src ./src
RUN chmod +x ./mvnw
RUN mvn dependency:go-offline  # Cache dependencies
RUN mvn package -DskipTests

# Runtime stage
FROM mcr.microsoft.com/openjdk/jdk:21-mariner
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "app.jar"]
