package com.finhub.search;

import com.finhub.search.repository.FinancialProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinhubSearchApplicationTests {

    @MockBean
    FinancialProductRepository financialProductRepository;

    @Test
    void contextLoads() {
    }

}
