package com.finhub.insurance;

import com.finhub.insurance.dto.event.InsuranceSubscribedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinhubInsuranceApplicationTests {

    @MockBean
    KafkaTemplate<String, InsuranceSubscribedEvent> kafkaTemplate;

    @Test
    void contextLoads() {
    }
}
