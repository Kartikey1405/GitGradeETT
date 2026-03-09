# ========================
# Stage 1: Build the JAR
# ========================
# We use a Maven image to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies (caches this layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application (skipping tests to speed up deployment)
RUN mvn clean package -DskipTests

# ========================
# Stage 2: Run the App
# ========================
# We use a lightweight Java Runtime (JRE) for the final image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/GitGradeSpring-0.0.1-SNAPSHOT.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# The command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]