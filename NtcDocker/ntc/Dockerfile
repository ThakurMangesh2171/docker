
# Use an official OpenJDK runtime as a parent image
FROM openjdk:11


# Set the working directory in the container
WORKDIR /app

# Copy the packaged JAR file into the container
COPY /target/gp-0.0.1-SNAPSHOT.jar /app/gp-0.0.1-SNAPSHOT.jar

# Expose the port your Spring Boot application will run on
EXPOSE 9090

# Define the command to run your Spring Boot application
CMD ["java", "-jar", "gp-0.0.1-SNAPSHOT.jar"]

