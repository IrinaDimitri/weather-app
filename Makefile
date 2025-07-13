ALL: run
.PHONY: run stop

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

stop:
	@echo "🛑 Остановка приложения..."
	docker compose -f docker/docker-compose.yml down
	mvn clean
	@find . -type f -name "*.log" -exec sh -c 'echo -n > "{}"' \;
	@echo "✅ Приложение остановлено"