package com.finhub.search.config;

import com.finhub.search.document.FinancialProductDocument;
import com.finhub.search.repository.FinancialProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final FinancialProductRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Elasticsearch 더미데이터 초기화 시작...");

        repository.deleteAll();

        List<FinancialProductDocument> documents = List.of(
                // 계좌 상품 3개
                FinancialProductDocument.builder()
                        .id("account-1")
                        .type("ACCOUNT")
                        .name("급여 자유입출금 통장")
                        .description("급여 이체 시 우대금리를 제공하는 자유입출금 통장입니다.")
                        .extraInfo("금리: 연 0.1%, 수수료 면제")
                        .build(),
                FinancialProductDocument.builder()
                        .id("account-2")
                        .type("ACCOUNT")
                        .name("정기예금 12개월")
                        .description("12개월 만기 확정금리 정기예금 상품입니다.")
                        .extraInfo("금리: 연 3.5%, 만기 일시 지급")
                        .build(),
                FinancialProductDocument.builder()
                        .id("account-3")
                        .type("ACCOUNT")
                        .name("청년 적금")
                        .description("만 19~34세 청년을 위한 고금리 적금 상품입니다.")
                        .extraInfo("금리: 연 6.0%, 월 최대 50만원")
                        .build(),

                // 주식 종목 5개
                FinancialProductDocument.builder()
                        .id("stock-1")
                        .type("STOCK")
                        .name("삼성전자")
                        .description("글로벌 반도체 및 전자기기 제조 기업입니다.")
                        .extraInfo("티커: 005930, 시장: KOSPI")
                        .build(),
                FinancialProductDocument.builder()
                        .id("stock-2")
                        .type("STOCK")
                        .name("애플")
                        .description("아이폰, 맥북 등 프리미엄 전자기기를 제조하는 글로벌 기업입니다.")
                        .extraInfo("티커: AAPL, 시장: NASDAQ")
                        .build(),
                FinancialProductDocument.builder()
                        .id("stock-3")
                        .type("STOCK")
                        .name("테슬라")
                        .description("전기차 및 에너지 저장 솔루션을 제공하는 기업입니다.")
                        .extraInfo("티커: TSLA, 시장: NASDAQ")
                        .build(),
                FinancialProductDocument.builder()
                        .id("stock-4")
                        .type("STOCK")
                        .name("네이버")
                        .description("국내 1위 인터넷 포털 및 IT 플랫폼 기업입니다.")
                        .extraInfo("티커: 035420, 시장: KOSPI")
                        .build(),
                FinancialProductDocument.builder()
                        .id("stock-5")
                        .type("STOCK")
                        .name("카카오")
                        .description("카카오톡 기반의 종합 IT 플랫폼 기업입니다.")
                        .extraInfo("티커: 035720, 시장: KOSPI")
                        .build(),

                // 보험 상품 5개
                FinancialProductDocument.builder()
                        .id("insurance-1")
                        .type("INSURANCE")
                        .name("삼성생명 종신보험")
                        .description("평생 보장되는 종신보험으로 사망 시 보험금을 지급합니다.")
                        .extraInfo("카테고리: LIFE, 월 보험료: 50,000원")
                        .build(),
                FinancialProductDocument.builder()
                        .id("insurance-2")
                        .type("INSURANCE")
                        .name("DB손보 암보험")
                        .description("주요 암 진단 시 진단비를 일시금으로 지급합니다.")
                        .extraInfo("카테고리: HEALTH, 월 보험료: 25,000원")
                        .build(),
                FinancialProductDocument.builder()
                        .id("insurance-3")
                        .type("INSURANCE")
                        .name("현대해상 실손보험")
                        .description("실제 발생한 의료비를 최대 90%까지 보장하는 실손 의료보험입니다.")
                        .extraInfo("카테고리: HEALTH, 월 보험료: 15,000원")
                        .build(),
                FinancialProductDocument.builder()
                        .id("insurance-4")
                        .type("INSURANCE")
                        .name("삼성화재 자동차보험")
                        .description("대인, 대물, 자차를 종합 보장하는 자동차보험입니다.")
                        .extraInfo("카테고리: AUTO, 월 보험료: 80,000원")
                        .build(),
                FinancialProductDocument.builder()
                        .id("insurance-5")
                        .type("INSURANCE")
                        .name("메리츠화재 여행자보험")
                        .description("국내외 여행 중 사고 및 질병을 보장하는 여행자보험입니다.")
                        .extraInfo("카테고리: HEALTH, 월 보험료: 5,000원")
                        .build()
        );

        repository.saveAll(documents);
        log.info("Search 더미데이터 {}건 삽입 완료", documents.size());
    }
}
