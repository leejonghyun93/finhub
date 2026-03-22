package com.finhub.payment;

import com.finhub.payment.dto.event.PaymentCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinhubPaymentApplicationTests {

    @MockBean
    KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Test
    void contextLoads() {
    }
}
