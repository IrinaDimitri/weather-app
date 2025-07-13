# Weather Application

## Оглавление
1. [Структура проекта](#структура-проекта)
2. [Сборка и запуск](#сборка-и-запуск)
3. [Функционал проекта](#функционал-проекта)
4. [Проверка методов](#проверка-методов)
5. [Подключение к PostgreSQL](#подключение-к-postgresql)
6. [Дополнительная информация](#дополнительная-информация)

## Структура проекта
```text
.
├── .dockerignore
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── LICENSE
├── docker
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── postgres.env
├── docs
│   ├── README.MD
│   └── Weather-app.postman_collection.json
├── mvnw
├── mvnw.cmd
├── nginx
│   ├── logs
│   │   ├── access.log
│   │   └── error.log
│   └── nginx.conf
├── pom.xml
└── src
    ├── logs
    │   └── weather-app.log
    └── main
        ├── java
        │   └── com
        │       └── example
        │           ├── WeatherApp.java
        │           ├── WeatherController.java
        │           ├── config
        │           │   └── SwaggerConfig.java
        │           ├── model
        │           │   └── Weather.java
        │           ├── repository
        │           │   └── WeatherRepository.java
        │           └── service
        │               └── WeatherService.java
        └── resources
            ├── application.properties
            └── data.sql
text```
Сборка и запуск
Требования
Docker и Docker Compose

JDK 17+

Maven 3.8+

Инструкция
bash
# Клонирование и сборка
git clone <ваш-репозиторий>
cd <папка-проекта>

# Запуск
mvn clean package
docker-compose -f docker/docker-compose.yml down
docker-compose -f docker/docker-compose.yml up -d --build
Приложение доступно по адресу: http://localhost

Функционал проекта
Основные методы API
Метод	Путь	Описание
GET	/health	Проверка работоспособности
GET	/ping	Тестовый эндпоинт
GET	/list	Список городов с температурами
POST	/add	Добавление/обновление города
DELETE	/delete	Удаление данных
GET	/update	Обновление температур
Проверка методов
Через браузер
http://localhost/ping

http://localhost/list

http://localhost/health

Через curl
bash
# Health check
curl http://localhost/health

# Добавить город
curl -X POST -d "city=Moscow&temperature=25" http://localhost/add

# Удалить все данные
curl -X DELETE "http://localhost/delete?all=true"
Postman
Импортируйте коллекцию: docs/Weather-app.postman_collection.json

Подключение к PostgreSQL
bash
docker exec -it weather-postgres psql -U postgres -d weather_db
Пример запросов:

sql
SELECT * FROM weather;
DELETE FROM weather WHERE city = 'Paris';
Дополнительная информация
Логи
Приложение: src/logs/weather-app.log

Nginx: nginx/logs/access.log и error.log

Управление сервисами
bash
# Остановка
docker-compose -f docker/docker-compose.yml down

# Просмотр логов
docker logs weather-backend
Лицензия: MIT (см. файл LICENSE)