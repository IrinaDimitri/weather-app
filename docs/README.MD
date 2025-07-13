# Weather Application

Простое веб-приложение для отображения данных о погоде с PostgreSQL, Nginx и Docker Compose.

## Содержание
1. Требования
2. Установка
3. Использование
4. Проверка работы
5. Дополнительно

## 1. Требования
- Docker
- Docker Compose

## 2. Установка
1. Клонируйте репозиторий:
git clone <ваш-репозиторий>
cd <папка-проекта>

2. Запустите приложение:
docker-compose -f docker/docker-compose.yml up --build

## 3. Использование
Приложение доступно по адресу: http://localhost

Основные эндпоинты:
- GET /ping    → "PONG" (HTML)
- GET /health  → {"status": "HEALTHY"} (JSON)
- GET /list    → список городов с температурой (HTML)
- POST /add    → добавление нового города (параметры: city, temperature)

## 4. Проверка работы
Примеры запросов:
curl http://localhost/ping
curl http://localhost/health
curl http://localhost/list
curl -X POST -d "city=Moscow&temperature=25" http://localhost/add

## 5. Дополнительно
- Проверка БД: docker exec -it weather-postgres psql -U postgres -d weather_db
- Логи: docker logs weather-backend
- Остановка: docker-compose -f docker/docker-compose.yml down

Лицензия: MIT