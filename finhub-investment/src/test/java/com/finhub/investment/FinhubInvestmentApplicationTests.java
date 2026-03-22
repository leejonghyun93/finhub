package com.finhub.investment;

import com.finhub.investment.dto.event.TradeCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinhubInvestmentApplicationTests {

    @MockBean
    KafkaTemplate<String, TradeCompletedEvent> kafkaTemplate;

    @Test
    void contextLoads() {
    }
}
