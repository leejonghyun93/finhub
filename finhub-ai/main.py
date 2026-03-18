from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import py_eureka_client.eureka_client as eureka_client

from core.config import settings
from core.database import init_db
from routers import chat, analysis, recommend


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 시작 시
    init_db()

    await eureka_client.init_async(
        eureka_server=settings.EUREKA_SERVER_URL,
        app_name=settings.APP_NAME,
        instance_port=settings.APP_PORT,
    )

    yield

    # 종료 시
    await eureka_client.stop_async()


app = FastAPI(
    title="FinHub AI Service",
    description="LangChain + LangGraph + Ollama 기반 AI 금융 어시스턴트",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router)
app.include_router(analysis.router)
app.include_router(recommend.router)


@app.get("/health")
async def health():
    return {"status": "UP", "service": settings.APP_NAME}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=settings.APP_PORT, reload=True)
