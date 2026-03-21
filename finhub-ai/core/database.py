from sqlalchemy import create_engine, Column, Integer, String, Text, text
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from pgvector.sqlalchemy import Vector
from core.config import settings

engine = create_engine(settings.DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


class FinancialProduct(Base):
    __tablename__ = "financial_products"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(200), nullable=False)
    category = Column(String(50), nullable=False)  # STOCK, INSURANCE, SAVINGS, LOAN
    description = Column(Text, nullable=False)
    embedding = Column(Vector(768), nullable=True)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


SEED_PRODUCTS = [
    ("KB 스타트적금", "적금", "월 최대 50만원 납입, 연 4.5% 금리, 1~3년 만기 선택 가능. 자동이체 시 우대금리 0.3% 추가 제공."),
    ("신한 청년희망적금", "적금", "만 19~34세 청년 대상, 연 6.0% 금리(정부 지원 포함), 2년 만기, 월 50만원 한도."),
    ("하나 자유적금", "적금", "자유 납입식 적금, 연 3.8% 금리, 최소 1만원부터 납입 가능, 중도해지 시 일부 이자 지급."),
    ("KODEX 200 ETF", "ETF", "코스피200 지수 추종 ETF, 낮은 보수(연 0.15%), 국내 대형주 분산투자, 실시간 매매 가능."),
    ("TIGER 미국S&P500 ETF", "ETF", "미국 S&P500 지수 추종, 연 보수 0.07%, 환헤지 없음, 달러 자산 분산투자 효과."),
    ("KODEX 채권혼합 ETF", "ETF", "주식 30% + 채권 70% 혼합형, 안정적 수익 추구, 연 보수 0.25%, 중위험 중수익."),
    ("삼성생명 종신보험", "보험", "사망 시 보험금 지급, 납입 기간 20년, 보장 기간 종신, 해지환급금 적립 기능 포함."),
    ("현대해상 실손의료보험", "보험", "입원·통원 의료비 실손 보장, 월 보험료 3~5만원대, 자기부담금 20%, 3년 갱신형."),
    ("DB손해보험 운전자보험", "보험", "교통사고 형사합의금·벌금 보장, 월 1~2만원, 운전 중 상해 및 사망 보장 포함."),
    ("KB국민은행 주택담보대출", "대출", "LTV 최대 70%, 금리 연 4.2~5.8%, 최장 30년, 원리금균등상환 방식, 중도상환수수료 없음(3년 후)."),
    ("카카오뱅크 신용대출", "대출", "비대면 신청, 연 5.5~12%, 최대 1억원, 1년 만기 자동연장, DSR 40% 이내."),
    ("삼성증권 CMA 통장", "저축", "하루만 맡겨도 이자 지급, 연 3.5% 내외, 입출금 자유, 증권사 우대 서비스 연계."),
]


def _generate_embeddings():
    """Ollama nomic-embed-text로 상품 임베딩 생성 및 저장 (embedding IS NULL인 상품만)"""
    from langchain_ollama import OllamaEmbeddings

    embed_model = OllamaEmbeddings(
        model=settings.OLLAMA_EMBED_MODEL,
        base_url=settings.OLLAMA_BASE_URL,
    )

    db = SessionLocal()
    try:
        products = db.query(FinancialProduct).filter(FinancialProduct.embedding.is_(None)).all()
        if not products:
            print("모든 상품에 임베딩이 이미 존재합니다.")
            return

        print(f"임베딩 생성 시작: {len(products)}개 상품")
        texts = [f"{p.name} {p.category} {p.description}" for p in products]
        vectors = embed_model.embed_documents(texts)

        for product, vector in zip(products, vectors):
            product.embedding = vector

        db.commit()
        print(f"임베딩 생성 완료: {len(products)}개")
    except Exception as e:
        db.rollback()
        print(f"[경고] 임베딩 생성 실패 (Ollama 연결 확인 필요): {e}")
    finally:
        db.close()


def init_db():
    # pgvector 확장 활성화 (테이블 생성 전에 반드시 실행)
    with engine.connect() as conn:
        conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
        conn.commit()
    print("pgvector 확장 활성화 완료")

    Base.metadata.create_all(bind=engine)
    print("테이블 생성 완료")

    db = SessionLocal()
    try:
        if db.query(FinancialProduct).count() == 0:
            for name, category, description in SEED_PRODUCTS:
                db.add(FinancialProduct(name=name, category=category, description=description))
            db.commit()
            print(f"금융 상품 시드 데이터 {len(SEED_PRODUCTS)}건 삽입 완료")
    finally:
        db.close()

    # 임베딩 생성 (Ollama 미기동 시 경고만 출력하고 계속 진행)
    _generate_embeddings()
