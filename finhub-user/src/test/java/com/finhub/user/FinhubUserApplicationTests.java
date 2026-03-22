package com.finhub.user;

import com.finhub.user.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinhubUserApplicationTests {

    @MockBean
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void contextLoads() {
    }
}
