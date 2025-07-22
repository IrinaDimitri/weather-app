# Weather API 

Spring Boot приложение для управления данными о погоде.  
Использует PostgreSQL, Swagger и поддерживает контейнеризацию через Docker.

## Содержание
1. [Структура проекта](#структура-проекта)
2. [Запуск проекта](#запуск-проекта)
   - [Требования](#требования)
   - [Сборка проекта](#сборка-проекта)
3. [Доступные эндпоинты](#доступные-эндпоинты)
   - [Swagger UI](#swagger-ui)
   - [Варианты тестирования](#варианты-тестирования)
   - [GET /ping](#get-ping)
   - [GET /health](#get-health)
   - [GET /list](#get-list)
   - [POST /add](#post-add)
   - [DELETE /delete](#delete-delete)
4. [Работа с PostgreSQL в Docker](#работа-с-postgresql-в-docker)
   - [Проверка подключения](#1-проверка-подключения-к-postgresql)
   - [Навигация по БД](#2-навигация-и-просмотр-структуры-базы-данных)
   - [Примеры SQL-запросов](#3-примеры-sql-запросов)
5. [Особенности проекта](#особенности-проекта)
   - [Прокси и порт в NGINX](#прокси-и-порт-в-nginx)
   - [Docker](#docker)
   - [Логирование](#логирование)
   - [GitHub Workflows](#github-workflows)
   - [Процесс сборки](#процесс-сборки-и-папка-target)
---
##  Структура проекта
```text
 weather-app
├── .dockerignore          # Игнорируемые файлы для Docker
├── .mvn/wrapper/          # Настройки Maven Wrapper
│   └── maven-wrapper.properties
├── Makefile               # Автоматизация сборки
├── all_files_combined.txt # (опционально) Объединённый лог
├── docker/                # Docker-конфигурации
│   ├── Dockerfile         # Образ приложения
│   └── docker-compose.yml # Оркестрация сервисов
├── nginx/                # Nginx прокси
│   └── nginx.conf        # Конфиг маршрутизации
├── pom.xml               # Зависимости Maven
└── src/main/
    ├── java/com/example/
    │   ├── WeatherApp.java       # Главный класс
    │   ├── WeatherController.java # REST API
    │   ├── config/
    │   │   └️ SwaggerConfig.java  # Документация
    │   ├️  model/Weather.java      # Сущность данных
    │   ├️ repository/WeatherRepository.java # Доступ к БД
    │   └️ service/WeatherService.java # Бизнес-логика
    └️ resources/
        ├️ application.properties # Настройки Spring
        └️ data.sql              # Инициализация БД
```

## Запуск проекта

### Требования
- Java версии 17+.
- Docker для контейнеризации компонентов (опционально).
- Maven — инструмент сборки и управления зависимостями.
- PostgreSQL база данных (локально или через Docker).

### Сборка проекта
Для запуска или рестарта в корневой директории проекта запустите команду:
```bash
make
```
Для того, чтобы остановить проект, запустите команду из корневой директории проекта:
```bash
make stop
```
Приложение будет доступно по адресам: http://localhost:8080 И через прокси http://localhost/

Вывести список всех доступных команд make можно с корневой директории проекта запросом
```bash
make help
```

# Доступные эндпоинты

## Swagger UI

Документация доступна по адресам после локальной сборки проекта:
- http://localhost/swagger-ui/index.html
- http://localhost:8080/swagger-ui/index.html

Также после сборки будет доступна OpenApi documentation: http://localhost/v3/api-docs

Открыть документацию из корневой директории проекта:
```bash
make doc
```

## Варианты тестирования
1. В Postman. Файл коллекции: docs/Weather-app.postman_collection.json
2. В браузере по прямым ссылкам http://localhost/{{method}}
3. Напрямую в терминале через curl http://localhost/{{method}}

## GET /ping  
*Функционал: проверка доступности сервиса.*

### Коды ответов:
| Код   | Описание      | Content-Type   | Пример ответа                                           |
|-------|---------------|----------------|---------------------------------------------------------|
| 200   | Сервис отвечает| text/html      | `<html><body><h1>PONG</h1></body></html>`             |

### Примеры тестирования:
| Способ    | Команда / URL                      |
|-----------|------------------------------------|
| Terminal  | `curl http://localhost/ping`       |
| REST API  | `GET http://localhost/ping`        |
| Browser   | `http://localhost/ping`            |

---

## GET /health  
*Функционал: проверка состояния сервиса.*

### Коды ответов:
| Код   | Описание    | Content-Type   | Пример ответа                                     |
|-------|-------------|----------------|---------------------------------------------------|
| 200   | Сервис здоров| application/json| `{ "status": "HEALTHY" }`                          |

### Примеры тестирования:
| Способ    | Команда / URL                       |
|-----------|-------------------------------------|
| Terminal  | `curl http://localhost/health`      |
| REST API  | `GET http://localhost/health`       |
| Browser   | `http://localhost/health`           |

---

## GET /list  
*Функционал: получение списка всех городов и температур.*

### Коды ответов:
| Код   | Описание                      | Content-Type   | Пример ответа                                                               |
|-------|-------------------------------|----------------|----------------------------------------------------------------------------|
| 200   | Список успешно возвращён       | text/html      | `<html><body><ul><li>Paris: 18°C</li><li>Rome: 25°C</li></ul></body></html>` |
| 204   | Список городов пуст            | text/html      | `<html><body><h2>Информация</h2><p>Список городов с погодой пока пуст.</p></body></html>` |

---

### Примеры тестирования:
| Способ    | Команда / URL                     |
|-----------|-----------------------------------|
| Terminal  | `curl http://localhost/list`      |
| REST API  | `GET http://localhost/list`       |
| Browser   | `http://localhost/list`           |

---

## POST /add  
*Функционал: добавление или обновление данных о погоде для города.*

### Параметры:
| Параметр     | Тип      | Обязательный | Описание                             |
|--------------|----------|--------------|--------------------------------------|
| `city`       | String   | ✅           | Название города (латиница, дефисы)   |
| `temperature`| Integer  | ❌           | Температура в °C (по умолчанию 20)   |
| `forceUpdate`| Boolean  | ❌           | Принудительное обновление            |

### Коды ответов:
| Код   | Описание                              | Content-Type   | Пример ответа                                                                                                 |
|-------|---------------------------------------|----------------|---------------------------------------------------------------------------------------------------------------|
| 200   | Город успешно добавлен или обновлён   | text/html      | `<html><body><h2>Успех!</h2><p>Город "Paris" добавлен с температурой 20°C.</p></body></html>`                   |
| 400   | Некорректные параметры                 | text/html      | `<html><body><h2>Ошибка!</h2><p>Параметр 'city' не должен быть пустым.</p></body></html>`                           |
| 409   | Город уже существует (без forceUpdate)| text/html      | `<html><body><h2>Ошибка!</h2><p>Город "London" уже существует в базе данных.</p></body></html>`                    |
| 500   | Внутренняя ошибка сервера             | text/html      | `<html><body><h2>Ошибка!</h2><p>Внутренняя ошибка сервера при сохранении данных.</p></body></html>`                 |

---

### Примеры тестирования:
| Сценарий             | Команда                                                        |
|----------------------|----------------------------------------------------------------|
| Добавление города Terminal      | `curl -X POST "http://localhost/add?city=Berlin"`              |
| Обновление города Terminal      | `curl -X POST "http://localhost/add?city=Berlin&forceUpdate=true"` |
| REST API              | `POST http://localhost/add?city=Berlin`                        |
| Browser               | `http://localhost/add?city=Berlin&forceUpdate=true`            |

---


## DELETE /delete  
*Функционал: удаление данных о погоде.*

### Параметры:
| Параметр | Тип      | Обязательный | Описание                  | Примечания                          |
|----------|----------|--------------|---------------------------|-------------------------------------|
| `city`   | String   | ❌           | Название города           | Нельзя использовать с `all=true`    |
| `all`    | Boolean  | ❌           | Удалить все данные        | Использовать `all=true`             |

> **ℹ️ Правила:**
> - Должен быть указан хотя бы один параметр.

### Коды ответов:
| Код   | Описание                               | Content-Type   | Пример ответа                                                                                                    |
|-------|----------------------------------------|----------------|------------------------------------------------------------------------------------------------------------------|
| 200   | Успешное удаление                      | text/html      | `<html><body><h2>Успех!</h2><p>Город "Rome" успешно удален.</p></body></html>`                                   |
| 400   | Не указан город и не установлен all=true| text/html      | `<html><body><h2>Ошибка!</h2><p>Необходимо указать 'city' или all=true.</p></body></html>`                      |
| 404   | Город не найден                        | text/html      | `<html><body><h2>Ошибка!</h2><p>Город "Atlantis" не найден в базе данных.</p></body></html>`                      |
| 500   | Внутренняя ошибка сервера              | text/html      | `<html><body><h2>Ошибка!</h2><p>Ошибка при удалении данных.</p></body></html>`                                |

---

### Примеры тестирования:
| Сценарий              | Команда                                                   |
|-----------------------|-----------------------------------------------------------|
| Удаление города Terminal        | `curl -X DELETE "http://localhost/delete?city=Berlin"`     |
| REST API              | `DELETE http://localhost/delete?city=Berlin`              |
| Browser               | `http://localhost/delete?city=Berlin`                     |
| Удаление всех Terminal        | `curl -X DELETE "http://localhost/delete?all=true"`        |
| Browser (все данные)  | `http://localhost/delete?all=true`                         |


# Работа с PostgreSQL в Docker

*Инструкция по подключению к базе данных PostgreSQL в Docker-контейнере и выполнению базовых SQL-команд.*

---

## 1. Проверка подключения к PostgreSQL

### 1.1 Проверить, что контейнер запущен

```bash
docker ps
```

Убедитесь, что контейнер с именем `weather-postgres` отображается в списке.

Запустить команду из корневой директории:
```bash
make db
```
В этом случае для выхода из weather_db=# нажмите \q

### 1.2 Подключиться напрямую к PostgreSQL из контейнера

```bash
docker exec -it weather-postgres psql -U postgres -d weather_db
```

Если подключение успешно, появится приглашение:

```
weather_db=#
```

### 1.3 Альтернатива: подключение через bash внутри контейнера

1. Войти в контейнер:
    ```bash
    docker exec -it weather-postgres bash
    ```

2. Затем подключиться к PostgreSQL:
    ```bash
    psql -U postgres -d weather_db
    ```

---

## 2. Навигация и просмотр структуры базы данных

### 2.1 Посмотреть список всех таблиц

```sql
\dt
```

### 2.2 Посмотреть структуру таблицы

```sql
\d weather
```

### 2.3 Посмотреть все записи в таблице `weather`

```sql
SELECT * FROM weather;
```

---

## 3. Примеры SQL-запросов

### 3.1 Добавить новую запись

```sql
INSERT INTO weather (city, temperature) VALUES ('Kazan', 24);
```

### 3.2 Удалить запись по идентификатору

```sql
DELETE FROM weather WHERE city = 'Kazan';
```

### 3.3 Обновить данные по условию

```sql
UPDATE weather SET temperature = 27 WHERE city = 'Kazan';
```

### 3.4 Найти записи с температурой выше определенного значения

```sql
SELECT * FROM weather WHERE temperature > 25;
```

---

## 4. Выход

### 4.1 Выйти из PostgreSQL

```sql
\q
```

### 4.2 Выйти из контейнера

```bash
exit
```

---
## Где хранятся данные

Данные о городах и их температурах хранятся в базе данных PostgreSQL, которая запускается в контейнере Docker. Изначально при первом запуске приложения, данные городов создаются через скрипт **`data.sql`**, который находится в папке **`src/main/resources`**.

Пример скрипта **`data.sql`**:
```sql
CREATE TABLE IF NOT EXISTS weather (
    city TEXT PRIMARY KEY,
    temperature INT
);

DELETE FROM weather;
INSERT INTO weather (city, temperature) VALUES 
  ('London', 20), 
  ('Paris', 15), 
  ('Moscow', 17);
```

# Особенности проекта

## Прокси и порт в NGINX

В проекте используется NGINX для проксирования запросов к серверу backend, где работает основное приложение. 

### Настройки NGINX:
- Конфигурация NGINX находится в **`nginx/nginx.conf`**.
- Прокси настроен так, что все запросы на **порт 80** (стандартный HTTP порт) перенаправляются на backend, который работает на **порт 8080**.

Пример конфигурации NGINX:
```nginx
# Определение группы серверов для балансировки нагрузки
upstream backend {
    server weather-backend:8080; #Проксирует на backend-контейнер
}

server {
    listen 80;# Слушает 80 порт
    server_name localhost; #Домен сервера
    access_log /var/log/nginx/access.log; # Лог доступа
    error_log /var/log/nginx/error.log; # Лог ошибок
    location / { 
        proxy_pass http://backend;# Перенаправление на backend
        proxy_set_header Host $host;# Оригинальный хост
        proxy_set_header X-Real-IP $remote_addr;# Реальный IP клиента
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;#Цепочка прокси
        proxy_set_header X-Forwarded-Proto $scheme;#Оригинальный протокол (http/https)
        proxy_connect_timeout 60s;# Таймаут подключения
        proxy_read_timeout 60s; #Таймаут чтения
    }

    location /swagger-ui/ {
        proxy_pass http://backend/swagger-ui/; 
    }

    location /v3/api-docs {
        proxy_pass http://backend/v3/api-docs; 
    }
}
```

### Детали работы:
- Внутри контейнера NGINX проксирует запросы, передавая их на backend сервер по адресу `weather-backend:8080`.
- Обработчик `/ping` отвечает на запросы с подтверждением работы сервиса, а `/health` возвращает статус сервиса.

## Docker

Проект использует Docker для контейнеризации приложения, что упрощает настройку и деплой. В проекте предусмотрены следующие контейнеры:
1. **Backend**: Контейнер с Spring Boot приложением, который обслуживает запросы.
2. **Postgres**: Контейнер с базой данных PostgreSQL для хранения информации о погоде.
3. **NGINX**: Контейнер с настроенным проксированием для перенаправления запросов на backend.

Контейнеры описаны в **`docker-compose.yml`** и **`Dockerfile`**, где указаны все необходимые параметры для их сборки и запуска.

## Логирование

Для логирования используется встроенное логирование Spring Boot. Логи приложения сохраняются в файл **`/app/logs/weather-app.log`**, который монтируется в Docker контейнере.

Пример конфигурации логирования:
```properties
logging.file.name=/app/logs/weather-app.log
```

## GitHub Workflows

Проект поддерживает автоматическое тестирование и деплой с использованием GitHub Actions. В репозитории настроены соответствующие workflow для автоматического тестирования кода при каждом push и деплоя приложения в облако.

### Пример workflow:
```yaml
name: Build and Deploy

on:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v2
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Deploy to Docker
        run: docker-compose up --build
```


## Логирование в NGINX

NGINX также ведёт логирование входящих запросов. Файлы логов монтируются из контейнера и находятся в директории:

```
nginx/logs/
├── access.log  # успешные запросы
├── error.log   # ошибки на уровне прокси
```

Эти логи можно использовать для:
- отслеживания проблем с маршрутизацией или доступностью backend;
- анализа активности и частоты запросов;
- выявления нестабильных endpoint'ов или периодов высокой нагрузки.

Примеры строк логов `access.log`:
```
127.0.0.1 - - [13/Jul/2025:12:00:01 +0000] "GET /ping HTTP/1.1" 200 32 "-" "curl/7.68.0"
127.0.0.1 - - [13/Jul/2025:12:01:01 +0000] "POST /add?city=Paris HTTP/1.1" 200 128 "-" "PostmanRuntime/7.32.0"
```

---

## Детали роутинга и проксирования

Вся маршрутизация организована через NGINX. Основная логика такова:

| Запрос (на порт 80)        | Проксируется на backend         | Комментарий                          |
|---------------------------|----------------------------------|--------------------------------------|
| `/`                       | `http://backend/`               | Главная страница, интерфейс          |
 JSON-индикатор работоспособности     |
| `/swagger-ui/`            | `http://backend/swagger-ui/`    | Swagger-документация                 |
| `/v3/api-docs`            | `http://backend/v3/api-docs`    | OpenAPI спецификация                 |

Всё это происходит благодаря следующей директиве в `nginx.conf`:

```nginx
    location / { 
        proxy_pass http://backend;# Перенаправление на backend
        proxy_set_header Host $host;# Оригинальный хост
        proxy_set_header X-Real-IP $remote_addr;# Реальный IP клиента
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;#Цепочка прокси
        proxy_set_header X-Forwarded-Proto $scheme;#Оригинальный протокол (http/https)
        proxy_connect_timeout 60s;# Таймаут подключения
        proxy_read_timeout 60s; #Таймаут чтения
    }
```

---

## Процесс сборки: роли ключевых файлов

| Файл                            | Назначение                                                        |
|--------------------------------|--------------------------------------------------------------------|
| `Dockerfile`                   | Инструкция по сборке backend-сервиса на основе JAR-файла          |
| `docker-compose.yml`           | Поднимает все сервисы: backend, Postgres, NGINX                   |
| `pom.xml`                      | Описывает зависимости и плагины для Maven сборки                  |
| `application.properties`       | Настройки приложения, включая логирование, интервал обновлений    |
| `data.sql`                     | SQL-инициализация базы данных при первом запуске                  |
| `nginx.conf`                   | Конфигурация прокси-сервера NGINX                                 |
| `WeatherApp.java`              | Точка входа Spring Boot                                           |
| `WeatherService.java`          | Основная бизнес-логика обновлений и работы с Open-Meteo API       |
| `WeatherController.java`       | Обработка HTTP-запросов                                           |

---

## Процесс сборки и папка `/target`

Проект использует **Maven** как систему сборки. В процессе компиляции и упаковки приложение проходит через несколько этапов, в результате чего формируется папка `target`.

### Как создаётся `/target`

1. **Команда:**

    ```bash
    mvn clean install
    ```

    - `clean`: удаляет старую папку `target`, если она существует
    - `install`: компилирует, запускает тесты, создает `.jar` и копирует его в локальный Maven-репозиторий

2. **После сборки:** Maven автоматически создаёт папку `target/`, в которой оказывается:
    - Скомпилированный код (в `classes/`)
    - Сгенерированный JAR (например, `weather-app-1.0.0.jar`)
    - Все промежуточные результаты сборки

### Роль `/target` в Docker

В `Dockerfile` используется JAR-файл из `target/`:

```Dockerfile
COPY --from=builder /app/target/*.jar app.jar
```

Таким образом:
- Сначала происходит сборка через Maven в контейнере `builder`
- Потом итоговый `.jar` копируется в финальный образ

---
