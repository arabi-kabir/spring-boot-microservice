FROM maven:3.9.9-eclipse-temurin-21 AS dev

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Copy the source code
COPY src src

# Build the project
RUN mvn clean install -DskipTests

# Run Spring Boot in exploded mode
CMD ["mvn", "spring-boot:run"]