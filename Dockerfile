# Stage 1: build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source
COPY src ./src

# Run tests first
RUN mvn test -Ptest -Dtest=!TodoAppApplicationTests

# Package the app (skip tests in prod build)
RUN mvn clean package -Pprod -DskipTests

# Stage 2: run with slim JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the packaged JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
