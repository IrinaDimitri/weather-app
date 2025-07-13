# Этап сборки с официальным образом Maven
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. Копируем только POM сначала (для кэширования зависимостей)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Копируем исходный код
COPY src ./src

# 3. Собираем проект
RUN mvn clean package -DskipTests -B

# Финальный образ с JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Копируем только собранный JAR
COPY --from=builder /app/target/*.jar app.jar

# Настройки
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]