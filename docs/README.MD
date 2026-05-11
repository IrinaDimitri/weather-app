```text
# Weather API

Spring Boot application for managing weather data.  
Uses PostgreSQL, Swagger, and supports containerization via Docker.

## Table of Contents
1. [Project Structure](#project-structure)
2. [Getting Started](#getting-started)
   - [Requirements](#requirements)
   - [Build](#build)
3. [Available Endpoints](#available-endpoints)
   - [Swagger UI](#swagger-ui)
   - [Testing Options](#testing-options)
   - [GET /ping](#get-ping)
   - [GET /health](#get-health)
   - [GET /list](#get-list)
   - [POST /add](#post-add)
   - [DELETE /delete](#delete-delete)
4. [Working with PostgreSQL in Docker](#working-with-postgresql-in-docker)
   - [Connection Check](#1-connection-check)
   - [Database Navigation](#2-database-navigation-and-structure)
   - [SQL Query Examples](#3-sql-query-examples)
5. [Project Features](#project-features)
   - [NGINX Proxy & Port](#nginx-proxy--port)
   - [Docker](#docker)
   - [Logging](#logging)
   - [GitHub Workflows](#github-workflows)
   - [Build Process](#build-process-and-target-folder)
---
## Project Structure
```text
 weather-app
├── .dockerignore          # Files ignored by Docker
├── .mvn/wrapper/          # Maven Wrapper settings
│   └── ⚙️ maven-wrapper.properties
├── Makefile               # Build automation
├── all_files_combined.txt # (optional) Combined log
├── docker/                # Docker configurations
│   ├── Dockerfile         # Application image
│   └── docker-compose.yml # Service orchestration
├── nginx/                # Nginx proxy
│   └── nginx.conf        # Routing config
├── pom.xml               # Maven dependencies
└── src/main/
    ├── java/com/example/
    │   ├── WeatherApp.java       # Main class
    │   ├── WeatherController.java # REST API
    │   ├── config/
    │   │   └️ SwaggerConfig.java  # Documentation
    │   ├️  model/Weather.java      # Data entity
    │   ├️ repository/WeatherRepository.java # Database access
    │   └️ service/WeatherService.java # Business logic
    └️ resources/
        ├️ application.properties # Spring settings
        └️ data.sql              # Database initialization
```

## Getting Started

### Requirements
- Java 17+.
- Docker for component containerization (optional).
- Maven — build and dependency management tool.
- PostgreSQL database (locally or via Docker).

### Build
To start or restart, run the following command from the project root directory:
```bash
make
```
To stop the project, run the following command from the project root directory:
```bash
make stop
```
The application will be available at: http://localhost:8080 and via proxy at http://localhost/

To list all available make commands from the project root directory:
```bash
make help
```

# Available Endpoints

## Swagger UI

Documentation is available at the following addresses after local build:
- http://localhost/swagger-ui/index.html
- http://localhost:8080/swagger-ui/index.html

After build, OpenAPI documentation will also be available at: http://localhost/v3/api-docs

To open documentation from the project root directory:
```bash
make doc
```

## Testing Options
1. Using Postman. Collection file: docs/Weather-app.postman_collection.json
2. In a browser via direct links: http://localhost/{{method}}
3. Directly in terminal via curl: curl http://localhost/{{method}}

## GET /ping  
*Functionality: service availability check.*

### Response Codes:
| Code | Description      | Content-Type   | Response Example                                         |
|------|------------------|----------------|----------------------------------------------------------|
| 200  | Service responds | text/html      | `<html><body><h1>PONG</h1></body></html>`              |

### Testing Examples:
| Method   | Command / URL                      |
|----------|------------------------------------|
| Terminal | `curl http://localhost/ping`       |
| REST API | `GET http://localhost/ping`        |
| Browser  | `http://localhost/ping`            |

---

## GET /health  
*Functionality: service health check.*

### Response Codes:
| Code | Description   | Content-Type     | Response Example                               |
|------|---------------|------------------|-------------------------------------------------|
| 200  | Service healthy| application/json | `{ "status": "HEALTHY" }`                       |

### Testing Examples:
| Method   | Command / URL                       |
|----------|-------------------------------------|
| Terminal | `curl http://localhost/health`      |
| REST API | `GET http://localhost/health`       |
| Browser  | `http://localhost/health`           |

---

## GET /list  
*Functionality: get a list of all cities and temperatures.*

### Response Codes:
| Code | Description                   | Content-Type | Response Example                                                            |
|------|-------------------------------|--------------|-----------------------------------------------------------------------------|
| 200  | List successfully returned    | text/html    | `<html><body><ul><li>Paris: 18°C</li><li>Rome: 25°C</li></ul></body></html>` |
| 204  | City list is empty             | text/html    | `<html><body><h2>Info</h2><p>The weather city list is currently empty.</p></body></html>` |

---

### Testing Examples:
| Method   | Command / URL                     |
|----------|-----------------------------------|
| Terminal | `curl http://localhost/list`      |
| REST API | `GET http://localhost/list`       |
| Browser  | `http://localhost/list`           |

---

## POST /add  
*Functionality: add or update weather data for a city.*

### Parameters:
| Parameter    | Type     | Required | Description                           |
|--------------|----------|----------|---------------------------------------|
| `city`       | String   | ✅        | City name (Latin letters, hyphens)    |
| `temperature`| Integer  | ❌        | Temperature in °C (default 20)        |
| `forceUpdate`| Boolean  | ❌        | Force update even if city exists      |

### Response Codes:
| Code | Description                             | Content-Type | Response Example                                                                                                  |
|------|-----------------------------------------|--------------|--------------------------------------------------------------------------------------------------------------------|
| 200  | City successfully added or updated      | text/html    | `<html><body><h2>Success!</h2><p>City "Paris" added with temperature 20°C.</p></body></html>`                    |
| 400  | Invalid parameters                      | text/html    | `<html><body><h2>Error!</h2><p>'city' parameter must not be empty.</p></body></html>`                           |
| 409  | City already exists (without forceUpdate)| text/html    | `<html><body><h2>Error!</h2><p>City "London" already exists in the database.</p></body></html>`                  |
| 500  | Internal server error                   | text/html    | `<html><body><h2>Error!</h2><p>Internal server error while saving data.</p></body></html>`                        |

---

### Testing Examples:
| Scenario                  | Command                                                        |
|---------------------------|----------------------------------------------------------------|
| Add city (Terminal)       | `curl -X POST "http://localhost/add?city=Berlin"`              |
| Update city (Terminal)    | `curl -X POST "http://localhost/add?city=Berlin&forceUpdate=true"` |
| REST API                  | `POST http://localhost/add?city=Berlin`                        |
| Browser                   | `http://localhost/add?city=Berlin&forceUpdate=true`            |

---


## DELETE /delete  
*Functionality: delete weather data.*

### Parameters:
| Parameter | Type     | Required | Description              | Notes                               |
|-----------|----------|----------|--------------------------|-------------------------------------|
| `city`    | String   | ❌        | City name               | Cannot be used with `all=true`      |
| `all`     | Boolean  | ❌        | Delete all data          | Use `all=true`                      |

> **ℹ️ Rules:**
> - At least one parameter must be specified.

### Response Codes:
| Code | Description                                | Content-Type | Response Example                                                                                                 |
|------|--------------------------------------------|--------------|-------------------------------------------------------------------------------------------------------------------|
| 200  | Successfully deleted                       | text/html    | `<html><body><h2>Success!</h2><p>City "Rome" successfully deleted.</p></body></html>`                             |
| 400  | No city specified and all=true not set     | text/html    | `<html><body><h2>Error!</h2><p>You must specify 'city' or set all=true.</p></body></html>`                      |
| 404  | City not found                             | text/html    | `<html><body><h2>Error!</h2><p>City "Atlantis" not found in the database.</p></body></html>`                      |
| 500  | Internal server error                      | text/html    | `<html><body><h2>Error!</h2><p>Error deleting data.</p></body></html>`                                          |

---

### Testing Examples:
| Scenario               | Command                                                   |
|------------------------|-----------------------------------------------------------|
| Delete city (Terminal) | `curl -X DELETE "http://localhost/delete?city=Berlin"`     |
| REST API               | `DELETE http://localhost/delete?city=Berlin`              |
| Browser                | `http://localhost/delete?city=Berlin`                     |
| Delete all (Terminal)  | `curl -X DELETE "http://localhost/delete?all=true"`        |
| Browser (all data)     | `http://localhost/delete?all=true`                         |


# Working with PostgreSQL in Docker

*Instructions for connecting to the PostgreSQL database in a Docker container and executing basic SQL commands.*

---

## 1. Connection Check

### 1.1 Verify the container is running

```bash
docker ps
```

Ensure the container named `weather-postgres` appears in the list.

Run the command from the root directory:
```bash
make db
```
In this case, to exit from weather_db=# type \q

### 1.2 Connect directly to PostgreSQL from the container

```bash
docker exec -it weather-postgres psql -U postgres -d weather_db
```

If the connection is successful, the prompt will appear:

```
weather_db=#
```

### 1.3 Alternative: connect via bash inside the container

1. Enter the container:
    ```bash
    docker exec -it weather-postgres bash
    ```

2. Then connect to PostgreSQL:
    ```bash
    psql -U postgres -d weather_db
    ```

---

## 2. Database Navigation and Structure

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

## 3. SQL Query Examples

### 3.1 Insert a new record

```sql
INSERT INTO weather (city, temperature) VALUES ('Kazan', 24);
```

### 3.2 Delete a record by identifier

```sql
DELETE FROM weather WHERE city = 'Kazan';
```

### 3.3 Update data by condition

```sql
UPDATE weather SET temperature = 27 WHERE city = 'Kazan';
```

### 3.4 Find records with temperature above a certain value

```sql
SELECT * FROM weather WHERE temperature > 25;
```

---

## 4. Exit

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

City and temperature data is stored in the PostgreSQL database running in a Docker container. Initially, when the application starts for the first time, city data is created using the **`data.sql`** script located in the **`src/main/resources`** folder.

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

## NGINX Proxy & Port

The project uses NGINX to proxy requests to the backend server where the main application runs.

### NGINX Settings:
- The NGINX configuration is located in **`nginx/nginx.conf`**.
- The proxy is configured so that all requests on **port 80** (standard HTTP port) are forwarded to the backend running on **port 8080**.

Example NGINX configuration:
```nginx
# Define server group for load balancing
upstream backend {
    server weather-backend:8080; #Proxies to the backend container
}

server {
    listen 80;# Listens on port 80
    server_name localhost; #Server domain
    access_log /var/log/nginx/access.log; # Access log
    error_log /var/log/nginx/error.log; # Error log
    location / { 
        proxy_pass http://backend;# Forward to backend
        proxy_set_header Host $host;# Original host
        proxy_set_header X-Real-IP $remote_addr;# Client real IP
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;#Proxy chain
        proxy_set_header X-Forwarded-Proto $scheme;#Original protocol (http/https)
        proxy_connect_timeout 60s;# Connection timeout
        proxy_read_timeout 60s; #Read timeout
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
- Inside the NGINX container, requests are proxied and forwarded to the backend server at `weather-backend:8080`.
- The `/ping` handler responds with a confirmation of service availability, and `/health` returns the service status.

## Docker

The project uses Docker for application containerization, simplifying setup and deployment. The project includes the following containers:
1. **Backend**: Container with the Spring Boot application serving requests.
2. **Postgres**: Container with the PostgreSQL database for storing weather information.
3. **NGINX**: Container with configured proxying to forward requests to the backend.

Containers are described in **`docker-compose.yml`** and **`Dockerfile`**, which specify all necessary parameters for their build and launch.

## Logging

Spring Boot's built-in logging is used. Application logs are saved to the file **`/app/logs/weather-app.log`**, which is mounted in the Docker container.

Example logging configuration:
```properties
logging.file.name=/app/logs/weather-app.log
```

## GitHub Workflows

The project supports automated testing and deployment using GitHub Actions. The repository is set up with corresponding workflows for automatic code testing on every push and deploying the application to the cloud.

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
- tracking routing issues or backend availability problems;
- analyzing request activity and frequency;
- identifying unstable endpoints or periods of high load.

Example `access.log` lines:
```
127.0.0.1 - - [13/Jul/2025:12:00:01 +0000] "GET /ping HTTP/1.1" 200 32 "-" "curl/7.68.0"
127.0.0.1 - - [13/Jul/2025:12:01:01 +0000] "POST /add?city=Paris HTTP/1.1" 200 128 "-" "PostmanRuntime/7.32.0"
```

---

## Routing and Proxy Details

All routing is organized through NGINX. The main logic is as follows:

| Request (on port 80)      | Proxied to backend              | Comment                               |
|---------------------------|----------------------------------|---------------------------------------|
| `/`                       | `http://backend/`               | Main page, interface                  |
| `/ping`                   | `http://backend/ping`           | Health check indicator                |
| `/swagger-ui/`            | `http://backend/swagger-ui/`    | Swagger documentation                 |
| `/v3/api-docs`            | `http://backend/v3/api-docs`    | OpenAPI specification                 |

This is achieved through the following directive in `nginx.conf`:

```nginx
    location / { 
        proxy_pass http://backend;# Forward to backend
        proxy_set_header Host $host;# Original host
        proxy_set_header X-Real-IP $remote_addr;# Client real IP
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;#Proxy chain
        proxy_set_header X-Forwarded-Proto $scheme;#Original protocol (http/https)
        proxy_connect_timeout 60s;# Connection timeout
        proxy_read_timeout 60s; #Read timeout
    }
```

---

## Build Process: Key File Roles

| File                            | Purpose                                                            |
|---------------------------------|--------------------------------------------------------------------|
| `Dockerfile`                   | Instructions for building the backend service based on a JAR file  |
| `docker-compose.yml`           | Starts all services: backend, Postgres, NGINX                      |
| `pom.xml`                      | Describes dependencies and plugins for Maven build                 |
| `application.properties`       | Application settings, including logging, update interval           |
| `data.sql`                     | SQL initialization of the database on first startup                |
| `nginx.conf`                   | NGINX proxy server configuration                                   |
| `WeatherApp.java`              | Spring Boot entry point                                            |
| `WeatherService.java`          | Core business logic for updates and working with Open-Meteo API    |
| `WeatherController.java`       | HTTP request handling                                              |

---

## Build Process and `/target` folder

The project uses **Maven** as a build system. During compilation and packaging, the application goes through several stages, resulting in the creation of the `target` folder.

### How `/target` is Created

1. **Command:**

    ```bash
    mvn clean install
    ```

    - `clean`: deletes the old `target` folder if it exists
    - `install`: compiles, runs tests, creates a `.jar` and copies it to the local Maven repository

2. **After build:** Maven automatically creates the `target/` folder, which contains:
    - Compiled code (in `classes/`)
    - Generated JAR (e.g., `weather-app-1.0.0.jar`)
    - All intermediate build results

### Role of `/target` in Docker

The `Dockerfile` uses the JAR file from `target/`:

```Dockerfile
COPY --from=builder /app/target/*.jar app.jar
```

Thus:
- First, the build is performed via Maven in the `builder` container
- Then the final `.jar` is copied to the final image
```
