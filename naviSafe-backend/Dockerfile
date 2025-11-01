FROM gradle:7.5-jdk17 AS builder

WORKDIR /app

COPY gradlew /app/
COPY gradle /app/gradle/
COPY build.gradle /app/
COPY settings.gradle /app/

RUN chmod +x gradlew

COPY src /app/src

RUN ./gradlew clean build -x test

# Stage 2: Run stage
FROM bellsoft/liberica-openjdk-debian:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
