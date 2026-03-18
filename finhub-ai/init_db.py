"""
DB 초기화 및 더미 금융 상품 데이터 시딩
실행: py init_db.py  또는  python3 init_db.py
"""
from core.database import init_db, SessionLocal, FinancialProduct

PRODUCTS = [
    {"name": "FinHub 안심 적금", "category": "SAVINGS",
     "description": "연 4.5% 금리의 1년 만기 정기적금. 월 최소 1만원부터 최대 100만원까지 납입 가능. 원금 보장, 예금자보호법 적용."},
    {"name": "FinHub 청년 희망 적금", "category": "SAVINGS",
     "description": "만 19~34세 청년 대상 특별 우대 적금. 연 5.0% 금리, 정부 지원 이자 추가 지급. 월 50만원 한도."},
    {"name": "FinHub 주식형 펀드 A", "category": "STOCK",
     "description": "국내 코스피 200 지수를 추종하는 인덱스 펀드. 낮은 수수료(연 0.15%), 분산투자로 리스크 최소화. 최소 투자금 1만원."},
    {"name": "FinHub 글로벌 테크 펀드", "category": "STOCK",
     "description": "미국 나스닥 기술주 중심 액티브 펀드. 연평균 수익률 12% (과거 5년). 고위험 고수익, 환율 변동 리스크 있음."},
    {"name": "FinHub 배당주 펀드", "category": "STOCK",
     "description": "국내외 고배당주에 투자하는 배당형 펀드. 연 3~5% 배당수익 기대. 중위험 중수익, 안정적인 현금흐름 선호 투자자에게 적합."},
    {"name": "FinHub 실손 의료 보험", "category": "INSURANCE",
     "description": "입원 및 통원 의료비 80% 보장. 월 보험료 3만원부터. 암, 뇌졸중, 심근경색 등 3대 질병 특약 가능."},
    {"name": "FinHub 종신 생명 보험", "category": "INSURANCE",
     "description": "사망 시 1억원 보장. 납입기간 20년, 보장기간 종신. 저축 기능 포함, 해지 환급금 발생."},
    {"name": "FinHub 자동차 보험", "category": "INSURANCE",
     "description": "대인, 대물, 자차, 자손 종합 보장. 무사고 할인 최대 60%. 긴급출동 서비스 무료 제공."},
    {"name": "FinHub 신용 대출", "category": "LOAN",
     "description": "신용등급 1~3등급 대상 저금리 대출. 연 6.9~12.5% 금리, 최대 5천만원. 중도상환 수수료 없음. 당일 심사 및 지급."},
    {"name": "FinHub 전세 자금 대출", "category": "LOAN",
     "description": "전세 보증금의 최대 80% 대출 지원. 연 3.5~5.5% 금리. 2년 만기 후 연장 가능."},
]


def seed():
    print("DB 초기화 중...")
    init_db()

    db = SessionLocal()
    try:
        existing = db.query(FinancialProduct).count()
        if existing > 0:
            print(f"이미 {existing}개의 상품이 존재합니다. 시딩을 건너뜁니다.")
            return

        for product in PRODUCTS:
            db.add(FinancialProduct(**product))
        db.commit()
        print(f"완료: {len(PRODUCTS)}개 금융 상품 저장")
    finally:
        db.close()


if __name__ == "__main__":
    seed()
