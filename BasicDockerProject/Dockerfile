# Use an official Java runtime as a parent image
FROM openjdk:11

# Set the working directory in the container
WORKDIR /app

# Copy the Java source file into the container
COPY HelloJavaDocker.java /app/

# Compile the Java code
RUN javac HelloJavaDocker.java

# Run the Java application
CMD ["java", "HelloJavaDocker"]

