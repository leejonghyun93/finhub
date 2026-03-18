from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    APP_NAME: str = "finhub-ai"
    APP_PORT: int = 8087

    DATABASE_URL: str = "postgresql://postgres:postgres@localhost:5432/finhub_ai"

    OLLAMA_BASE_URL: str = "http://host.docker.internal:11434"
    OLLAMA_MODEL: str = "llama3.2"
    OLLAMA_EMBED_MODEL: str = "nomic-embed-text"

    JWT_SECRET: str = "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256"
    JWT_ALGORITHM: str = "HS256"

    EUREKA_SERVER_URL: str = "http://localhost:8761/eureka"


settings = Settings()
