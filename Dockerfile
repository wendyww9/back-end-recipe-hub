# ---- Stage 1: Build the application ----
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /back-end-recipe-hub

# Copy the project files to the container
COPY pom.xml .
COPY src ./src

# Build the application (skip tests to speed up build)
RUN mvn clean package -DskipTests

# ---- Stage 2: Create the runtime image ----
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /back-end-recipe-hub

# Copy the JAR file from the build stage
COPY --from=build /back-end-recipe-hub/target/*.jar app.jar

# Expose the application's port (adjust if needed)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]