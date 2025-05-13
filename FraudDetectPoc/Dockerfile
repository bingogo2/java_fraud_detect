# Use OpenJDK 11 as the base image. failed to get higher 17 nor 21.
FROM amazoncorretto:21


# Ensure we are running as root (default user is root, but for clarity we specify)
USER root

# Create the /app directory with proper permissions
RUN mkdir -p /app && chmod -R 755 /app

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the target directory to the container
COPY target/FraudDetectPoc-0.0.1-SNAPSHOT.jar /app/fraud-detection.jar

# Set proper permissions for the JAR file
RUN chmod 755 /app/fraud-detection.jar

# Expose port 8081
EXPOSE 8081

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/fraud-detection.jar"]
