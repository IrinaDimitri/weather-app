# Сборка с образом Maven
FROM maven:3.8.7-eclipse-temurin-17 AS builder

WORKDIR /app

# POM для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Сброка проекта
RUN mvn clean package -DskipTests -B

# Финальный образ с JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]