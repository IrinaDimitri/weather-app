# Weather API

Spring Boot application for managing weather data.
Uses PostgreSQL, Swagger and supports containerization via Docker.

## Table of Contents
1. [Project Structure](#project-structure)
2. [Getting Started](#getting-started)
   - [Requirements](#requirements)
   - [Building the Project](#building-the-project)
3. [Available Endpoints](#available-endpoints)
   - [Swagger UI](#swagger-ui)
   - [Testing Options](#testing-options)
   - [GET /ping](#get-ping)
   - [GET /health](#get-health)
   - [GET /list](#get-list)
   - [POST /add](#post-add)
   - [DELETE /delete](#delete-delete)
4. [Working with PostgreSQL in Docker](#working-with-postgresql-in-docker)
   - [Checking the Connection](#1-checking-the-postgresql-connection)
   - [Navigating the Database](#2-navigating-and-viewing-database-structure)
   - [SQL Query Examples](#3-example-sql-queries)
5. [Project Features](#project-features)
   - [NGINX Proxy and Port](#nginx-proxy-and-port)
   - [Docker](#docker)
   - [Logging](#logging)
   - [GitHub Workflows](#github-workflows)
   - [Build Process](#build-process-and-the-target-folder)
---
## Project Structure
```text
 weather-app
├── .dockerignore          # Files ignored by Docker
├── .mvn/wrapper/          # Maven Wrapper settings
│   └── ⚙️ maven-wrapper.properties
├── Makefile               # Build automation
├── all_files_combined.txt # (optional) Combined log file
├── docker/                # Docker configurations
│   ├── Dockerfile         # Application image
│   └── docker-compose.yml # Service orchestration
├── nginx/                # Nginx proxy
│   └── nginx.conf        # Routing configuration
├── pom.xml               # Maven dependencies
└── src/main/
    ├── java/com/example/
    │   ├── WeatherApp.java       # Main class
    │   ├── WeatherController.java # REST API
    │   ├── config/
    │   │   └️ SwaggerConfig.java  # API Documentation
    │   ├️  model/Weather.java      # Data entity
    │   ├️ repository/WeatherRepository.java # Database access
    │   └️ service/WeatherService.java # Business logic
    └️ resources/
        ├️ application.properties # Spring settings
        └️ data.sql              # Database initialization
```

## Getting Started

### Requirements
- Java version 17+.
- Docker for component containerization (optional).
- Maven — build and dependency management tool.
- PostgreSQL database (locally or via Docker).

### Building the Project
To start or restart, run the following command from the project root directory:
```bash
make
```
To stop the project, run this command from the project root directory:
```bash
make stop
```
The application will be available at: http://localhost:8080 and via the proxy at http://localhost/

To list all available make commands from the project root directory:
```bash
make help
```

# Available Endpoints

## Swagger UI

Documentation is available at the following addresses after a local build:
- http://localhost/swagger-ui/index.html
- http://localhost:8080/swagger-ui/index.html

After building, OpenAPI documentation will also be available at: http://localhost/v3/api-docs

To open the documentation from the project root directory:
```bash
make doc
```

## Testing Options
1. In Postman. Collection file: docs/Weather-app.postman_collection.json
2. In a browser using direct links: http://localhost/{{method}}
3. Directly in the terminal using curl: curl http://localhost/{{method}}

## GET /ping
*Functionality: Checks service availability.*

### Response Codes:
| Code | Description       | Content-Type   | Sample Response                                         |
|------|-------------------|----------------|---------------------------------------------------------|
| 200  | Service is responding | text/html      | `<html><body><h1>PONG</h1></body></html>`             |

### Testing Examples:
| Method   | Command / URL                    |
|----------|----------------------------------|
| Terminal | `curl http://localhost/ping`     |
| REST API | `GET http://localhost/ping`      |
| Browser  | `http://localhost/ping`          |

---

## GET /health
*Functionality: Checks service health.*

### Response Codes:
| Code | Description       | Content-Type     | Sample Response                                  |
|------|-------------------|------------------|--------------------------------------------------|
| 200  | Service is healthy| application/json | `{ "status": "HEALTHY" }`                        |

### Testing Examples:
| Method   | Command / URL                     |
|----------|-----------------------------------|
| Terminal | `curl http://localhost/health`    |
| REST API | `GET http://localhost/health`     |
| Browser  | `http://localhost/health`         |

---

## GET /list
*Functionality: Retrieves a list of all cities and temperatures.*

### Response Codes:
| Code | Description                     | Content-Type | Sample Response                                                               |
|------|---------------------------------|--------------|-------------------------------------------------------------------------------|
| 200  | List successfully retrieved     | text/html    | `<html><body><ul><li>Paris: 18°C</li><li>Rome: 25°C</li></ul></body></html>` |
| 204  | City list is empty              | text/html    | `<html><body><h2>Information</h2><p>The list of cities with weather is currently empty.</p></body></html>` |

---

### Testing Examples:
| Method   | Command / URL                   |
|----------|---------------------------------|
| Terminal | `curl http://localhost/list`    |
| REST API | `GET http://localhost/list`     |
| Browser  | `http://localhost/list`         |

---

## POST /add
*Functionality: Adds or updates weather data for a city.*

### Parameters:
| Parameter     | Type    | Required | Description                              |
|---------------|---------|----------|------------------------------------------|
| `city`        | String  | ✅       | City name (Latin characters, hyphens)    |
| `temperature` | Integer | ❌       | Temperature in °C (default: 20)          |
| `forceUpdate` | Boolean | ❌       | Forced update flag                       |

### Response Codes:
| Code | Description                                  | Content-Type | Sample Response                                                                                                       |
|------|----------------------------------------------|--------------|-----------------------------------------------------------------------------------------------------------------------|
| 200  | City successfully added or updated           | text/html    | `<html><body><h2>Success!</h2><p>The city "Paris" has been added with a temperature of 20°C.</p></body></html>`       |
| 400  | Invalid parameters                           | text/html    | `<html><body><h2>Error!</h2><p>The 'city' parameter must not be empty.</p></body></html>`                             |
| 409  | City already exists (without forceUpdate)    | text/html    | `<html><body><h2>Error!</h2><p>The city "London" already exists in the database.</p></body></html>`                   |
| 500  | Internal server error                        | text/html    | `<html><body><h2>Error!</h2><p>Internal server error while saving data.</p></body></html>`                            |

---

### Testing Examples:
| Scenario                     | Command                                                          |
|------------------------------|------------------------------------------------------------------|
| Add a city (Terminal)        | `curl -X POST "http://localhost/add?city=Berlin"`                |
| Update a city (Terminal)     | `curl -X POST "http://localhost/add?city=Berlin&forceUpdate=true"` |
| REST API                     | `POST http://localhost/add?city=Berlin`                          |
| Browser                      | `http://localhost/add?city=Berlin&forceUpdate=true`              |

---


## DELETE /delete
*Functionality: Deletes weather data.*

### Parameters:
| Parameter | Type    | Required | Description                    | Notes                                   |
|-----------|---------|----------|--------------------------------|-----------------------------------------|
| `city`    | String  | ❌       | City name                      | Cannot be used with `all=true`          |
| `all`     | Boolean | ❌       | Delete all data                | Use `all=true`                          |

> **ℹ️ Rules:**
> - At least one parameter must be specified.

### Response Codes:
| Code | Description                                  | Content-Type | Sample Response                                                                                                 |
|------|----------------------------------------------|--------------|-----------------------------------------------------------------------------------------------------------------|
| 200  | Successful deletion                          | text/html    | `<html><body><h2>Success!</h2><p>The city "Rome" has been successfully deleted.</p></body></html>`              |
| 400  | No city specified and all is not set to true | text/html    | `<html><body><h2>Error!</h2><p>You must specify 'city' or set all=true.</p></body></html>`                      |
| 404  | City not found                               | text/html    | `<html><body><h2>Error!</h2><p>The city "Atlantis" was not found in the database.</p></body></html>`            |
| 500  | Internal server error                        | text/html    | `<html><body><h2>Error!</h2><p>An error occurred while deleting data.</p></body></html>`                        |

---

### Testing Examples:
| Scenario                    | Command                                                     |
|-----------------------------|-------------------------------------------------------------|
| Delete a city (Terminal)    | `curl -X DELETE "http://localhost/delete?city=Berlin"`      |
| REST API                    | `DELETE http://localhost/delete?city=Berlin`                |
| Browser                     | `http://localhost/delete?city=Berlin`                       |
| Delete all data (Terminal)  | `curl -X DELETE "http://localhost/delete?all=true"`         |
| Browser (all data)          | `http://localhost/delete?all=true`                          |


# Working with PostgreSQL in Docker

*Instructions for connecting to the PostgreSQL database in a Docker container and running basic SQL commands.*

---

## 1. Checking the PostgreSQL Connection

### 1.1 Verify the container is running

```bash
docker ps
```

Ensure the container named `weather-postgres` appears in the list.

Run this command from the root directory:
```bash
make db
```
In this case, to exit `weather_db=#`, type `\q`

### 1.2 Connect directly to PostgreSQL from the container

```bash
docker exec -it weather-postgres psql -U postgres -d weather_db
```

If the connection is successful, the prompt will appear:

```
weather_db=#
```

### 1.3 Alternative: Connect via bash inside the container

1. Enter the container:
    ```bash
    docker exec -it weather-postgres bash
    ```

2. Then connect to PostgreSQL:
    ```bash
    psql -U postgres -d weather_db
    ```

---

## 2. Navigating and Viewing Database Structure

### 2.1 List all tables

```sql
\dt
```

### 2.2 View table structure

```sql
\d weather
```

### 2.3 View all records in the `weather` table

```sql
SELECT * FROM weather;
```

---

## 3. Example SQL Queries

### 3.1 Add a new record

```sql
INSERT INTO weather (city, temperature) VALUES ('Kazan', 24);
```

### 3.2 Delete a record by identifier

```sql
DELETE FROM weather WHERE city = 'Kazan';
```

### 3.3 Update data based on a condition

```sql
UPDATE weather SET temperature = 27 WHERE city = 'Kazan';
```

### 3.4 Find records with a temperature above a certain value

```sql
SELECT * FROM weather WHERE temperature > 25;
```

---

## 4. Exiting

### 4.1 Exit PostgreSQL

```sql
\q
```

### 4.2 Exit the container

```bash
exit
```

---
## Where Data is Stored

City and temperature data is stored in a PostgreSQL database running inside a Docker container. Upon the initial application startup, city data is created via the **`data.sql`** script located in the **`src/main/resources`** folder.

Example **`data.sql`** script:
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

# Project Features

## NGINX Proxy and Port

This project uses NGINX to proxy requests to the backend server running the main application.

### NGINX Configuration:
- The NGINX configuration is located in **`nginx/nginx.conf`**.
- The proxy is set up so that all requests on **port 80** (the standard HTTP port) are forwarded to the backend, which runs on **port 8080**.

Example NGINX configuration:
```nginx
# Defines a group of servers for load balancing
upstream backend {
    server weather-backend:8080; # Proxies to the backend container
}

server {
    listen 80; # Listens on port 80
    server_name localhost; # Server domain
    access_log /var/log/nginx/access.log; # Access log
    error_log /var/log/nginx/error.log; # Error log
    location / { 
        proxy_pass http://backend; # Forwards requests to backend
        proxy_set_header Host $host; # Original host
        proxy_set_header X-Real-IP $remote_addr; # Client's real IP
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; # Proxy chain
        proxy_set_header X-Forwarded-Proto $scheme; # Original protocol (http/https)
        proxy_connect_timeout 60s; # Connection timeout
        proxy_read_timeout 60s; # Read timeout
    }

    location /swagger-ui/ {
        proxy_pass http://backend/swagger-ui/; 
    }

    location /v3/api-docs {
        proxy_pass http://backend/v3/api-docs; 
    }
}
```

### How It Works:
- Inside the NGINX container, requests are proxied and passed to the backend server at the address `weather-backend:8080`.
- The `/ping` handler responds to requests confirming service operation, while `/health` returns the service status.

## Docker

The project uses Docker to containerize the application, simplifying setup and deployment. The following containers are provided:
1. **Backend**: The Spring Boot application container that serves requests.
2. **Postgres**: A PostgreSQL database container for storing weather information.
3. **NGINX**: A container with configured proxying to forward requests to the backend.

The containers are described in **`docker-compose.yml`** and **`Dockerfile`**, which specify all necessary parameters for building and running them.

## Logging

The built-in Spring Boot logging is used. Application logs are saved to **`/app/logs/weather-app.log`**, which is mounted within the Docker container.

Example logging configuration:
```properties
logging.file.name=/app/logs/weather-app.log
```

## GitHub Workflows

The project supports automated testing and deployment using GitHub Actions. Relevant workflows are configured in the repository to automatically test code on each push and deploy the application to the cloud.

### Example workflow:
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


## NGINX Logging

NGINX also logs incoming requests. Log files are mounted from the container and located in the directory:

```
nginx/logs/
├── access.log  # successful requests
├── error.log   # proxy-level errors
```

These logs can be used for:
- troubleshooting routing or backend availability issues;
- analyzing activity and request frequency;
- identifying unstable endpoints or high-load periods.

Example `access.log` entries:
```
127.0.0.1 - - [13/Jul/2025:12:00:01 +0000] "GET /ping HTTP/1.1" 200 32 "-" "curl/7.68.0"
127.0.0.1 - - [13/Jul/2025:12:01:01 +0000] "POST /add?city=Paris HTTP/1.1" 200 128 "-" "PostmanRuntime/7.32.0"
```

---

## Routing and Proxy Details

All routing is organized through NGINX. The core logic is as follows:

| Request (on port 80)      | Proxied to backend              | Comment                               |
|---------------------------|----------------------------------|---------------------------------------|
| `/`                       | `http://backend/`               | Main page, interface                  |
 JSON healthcheck indicator          |
| `/swagger-ui/`            | `http://backend/swagger-ui/`    | Swagger documentation                 |
| `/v3/api-docs`            | `http://backend/v3/api-docs`    | OpenAPI specification                 |

This is all achieved thanks to the following directive in `nginx.conf`:

```nginx
    location / { 
        proxy_pass http://backend; # Forwards requests to backend
        proxy_set_header Host $host; # Original host
        proxy_set_header X-Real-IP $remote_addr; # Client's real IP
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; # Proxy chain
        proxy_set_header X-Forwarded-Proto $scheme; # Original protocol (http/https)
        proxy_connect_timeout 60s; # Connection timeout
        proxy_read_timeout 60s; # Read timeout
    }
```

---

## Build Process: Roles of Key Files

| File                            | Purpose                                                              |
|---------------------------------|----------------------------------------------------------------------|
| `Dockerfile`                    | Instructions for building the backend service based on a JAR file    |
| `docker-compose.yml`            | Spins up all services: backend, Postgres, NGINX                      |
| `pom.xml`                       | Describes dependencies and plugins for the Maven build               |
| `application.properties`        | Application settings, including logging and refresh interval         |
| `data.sql`                      | SQL database initialization script on first startup                  |
| `nginx.conf`                    | NGINX proxy server configuration                                     |
| `WeatherApp.java`               | Spring Boot entry point                                              |
| `WeatherService.java`           | Core business logic for updates and interactions with the Open-Meteo API |
| `WeatherController.java`        | HTTP request handling                                                |

---

## Build Process and the `/target` Folder

The project uses **Maven** as a build system. During compilation and packaging, the application goes through several stages, resulting in a `target` folder.

### How `/target` is Created

1. **Command:**

    ```bash
    mvn clean install
    ```

    - `clean`: deletes the old `target` folder if it exists
    - `install`: compiles, runs tests, creates a `.jar`, and copies it to the local Maven repository

2. **After building:** Maven automatically creates the `target/` folder, which will contain:
    - Compiled code (in `classes/`)
    - The generated JAR (e.g., `weather-app-1.0.0.jar`)
    - All intermediate build outputs

### The Role of `/target` in Docker

The `Dockerfile` uses the JAR file from `target/`:

```Dockerfile
COPY --from=builder /app/target/*.jar app.jar
```

Therefore:
- First, the build is performed using Maven in a `builder` container
- Then, the final `.jar` is copied into the final image
```
