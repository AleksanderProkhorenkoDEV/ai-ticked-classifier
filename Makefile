.PHONY: up down logs backend frontend build clean

up:
	docker compose -f infraestructure/docker-compose.yaml --env-file .env up -d

down:
	docker compose -f infraestructure/docker-compose.yaml --env-file .env down

logs:
	docker compose -f infraestructure/docker-compose.yaml --env-file .env logs -f

clean:
	docker compose -f infraestructure/docker-compose.yaml --env-file .env down -v