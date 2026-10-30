# Builds and runs the SideTracked API in a container (used by Render).

# Step 1: build the app with the Maven wrapper.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Windows line endings would stop the wrapper script from running.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
# Tests need a database, so they run on your computer, not here.
RUN ./mvnw -q package -DskipTests

# Step 2: run it on a smaller Java image.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Fit inside a small (512 MB) free server.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
