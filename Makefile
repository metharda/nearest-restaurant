.PHONY: all frontend backend auth-service path-service queue-service stop clean

all: frontend backend

frontend:
	@echo "Frontend servisi başlatılıyor..."
	@cd apps/frontend && pnpm install && pnpm run dev &

backend: auth-service path-service queue-service

auth-service:
	@echo "Auth servisi başlatılıyor..."
	@cd apps/backend && ./gradlew AuthService:bootRun &

path-service:
	@echo "Path servisi başlatılıyor..."
	@cd apps/backend && ./gradlew PathService:bootRun &

queue-service:
	@echo "Queue servisi başlatılıyor..."
	@cd apps/backend && ./gradlew QueueService:bootRun &

stop:
	@echo "Tüm servisler durduruluyor..."
	@pkill -f "npm run dev" || true
	@pkill -f "gradle" || true

clean:
	@echo "Geçici dosyalar temizleniyor..."
	@find . -name "node_modules" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name ".next" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name "dist" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@find . -name "build" -type d -prune -exec rm -rf {} \; 2>/dev/null || true
	@cd apps/backend && ./gradlew clean