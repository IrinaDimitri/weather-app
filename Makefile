ALL: run
.PHONY: run stop db doc help

run:
	@echo "🚀 Запуск приложения..."
	
	# Проверка доступности Docker
	@if ! docker info >/dev/null 2>&1; then \
		echo "🛑 Docker Desktop не запущен!"; \
		echo "1. Запустите Docker Desktop"; \
		echo "2. Включите WSL2 интеграцию в настройках"; \
		exit 1; \
	fi
	
	mvn clean package
	docker-compose -f docker/docker-compose.yml down
	docker compose -f docker/docker-compose.yml up -d --build
	@echo "✅ Приложение запущено"
	@echo "📄 Документация: make doc"

stop:
	@echo "🛑 Остановка приложения..."
	docker compose -f docker/docker-compose.yml down
	mvn clean
	@find . -type f -name "*.log" -exec sh -c 'echo -n > "{}"' \;
	@echo "✅ Приложение остановлено"

db: 
	@echo "🛢️ Подключение к PostgreSQL..."
	docker exec -it weather-postgres psql -U postgres -d weather_db

doc:
	@echo "📄 Открытие Swagger UI в браузере..."
	@if ! docker compose -f docker/docker-compose.yml ps | grep -q "Up"; then \
		echo "🛑 Проект не запущен! Сначала выполните: make run"; \
		exit 1; \
	fi
	@xdg-open "http://localhost/swagger-ui/index.html" 2>/dev/null || \
	open "http://localhost/swagger-ui/index.html" 2>/dev/null || \
	echo "✖ Не удалось открыть браузер. Перейдите по ссылке вручную: http://localhost/swagger-ui/index.html"

help:
	@echo "📖 Доступные команды:"
	@echo "make run    - 🚀 Запустить приложение (сборка + контейнеры)"
	@echo "make stop   - 🛑 Остановить приложение (контейнеры + очистка)"
	@echo "make db     - 🛢️ Подключиться к PostgreSQL консоли"
	@echo "make doc    - 📄 Открыть Swagger UI в браузере"
	@echo "make help   - ℹ Показать эту справку"
	@echo ""
	@echo "🔗 После запуска:"
	@echo "• API: http://localhost"
	@echo "• Документация: http://localhost/swagger-ui/index.html"
	@echo "• База данных: порт 5432"