.PHONY: all build-all frontend build-frontend backend build-backend auth-service build-auth-service path-service build-path-service queue-service build-queue-service stop-all stop-frontend stop-backend clean

all: frontend backend

build-all: build-frontend build-backend

frontend:
	@echo "Frontend servisi başlatılıyor..."
	@cd apps/frontend && pnpm install && pnpm run dev & >/dev/null 2>&1 || true

build-frontend:
	@echo "Frontend buildleniyor..."
	@cd apps/frontend && pnpm install && pnpm run build & >/dev/null 2>&1 || true

backend: auth-service path-service queue-service

build-backend: build-auth-service build-path-service build-queue-service

auth-service:
	@echo "Auth servisi başlatılıyor..."
	@cd apps/backend && ./gradlew AuthService:bootRun -q & >/dev/null 2>&1 || true

build-auth-service:
	@echo "Auth servisi buildleniyor..."
	@cd apps/backend && ./gradlew AuthService:build -q & >/dev/null 2>&1 || true

path-service:
	@echo "Path servisi başlatılıyor..."
	@cd apps/backend && ./gradlew PathService:bootRun -q & >/dev/null 2>&1 || true

build-path-service:
	@echo "Path servisi buildleniyor..."
	@cd apps/backend && ./gradlew PathService:build -q & >/dev/null 2>&1 || true

queue-service:
	@echo "Queue servisi başlatılıyor..."
	@cd apps/backend && ./gradlew QueueService:bootRun -q & >/dev/null 2>&1 || true

build-queue-service:
	@echo "Queue servisi buildleniyor..."
	@cd apps/backend && ./gradlew QueueService:build -q & >/dev/null 2>&1 || true

stop-all: stop-frontend stop-backend

stop-frontend:
	@echo "Frontend durduruluyor..."
	@pkill -f "npm run dev" & >/dev/null 2>&1 || true

stop-backend:
	@echo "Backend servisler durduruluyor..."
	@cd apps/backend && ./gradlew AuthService:bootRun -q --stop && ./gradlew PathService:bootRun -q --stop && ./gradlew QueueService:bootRun -q --stop >/dev/null 2>&1 || true

clean:
	@echo "Geçici dosyalar temizleniyor..."
	@find . -name "node_modules" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name ".next" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name "dist" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name "build" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name "bin" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name ".gradle" -type d -prune -exec rm -rf {} \; 2>/dev/null || true