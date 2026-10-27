# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder
# Set the working directory inside the container
WORKDIR /app
 # Copy the pom.xml file to the working directory
COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
# Copy the source code to the working directory
COPY order-service/ order-service/ 
# Package only the order-service
RUN mvn clean package -pl order-service -am -DskipTests=true


# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/order-service/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
