# Stage 1: build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# First copy only pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Then copy the rest of the source
COPY src ./src

# Package the app (skip tests in prod builds)
RUN mvn clean package -Pprod -DskipTests

# Stage 2: run with slim JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy only the fat JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
