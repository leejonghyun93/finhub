import os

# pydantic-settings가 Settings()를 인스턴스화하기 전에 필수 환경변수를 설정한다.
# conftest.py는 pytest가 테스트 파일을 수집(import)하기 전에 실행된다.
os.environ.setdefault("JWT_SECRET", "test-secret-key-must-be-at-least-256-bits-long-for-hs256")
os.environ.setdefault("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/finhub_ai_test")
os.environ.setdefault("OLLAMA_BASE_URL", "http://localhost:11434")
os.environ.setdefault("OLLAMA_MODEL", "llama3.2")
os.environ.setdefault("OLLAMA_EMBED_MODEL", "nomic-embed-text")
