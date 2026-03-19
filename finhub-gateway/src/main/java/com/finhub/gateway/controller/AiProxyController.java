package com.finhub.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

@Slf4j
@RestController
public class AiProxyController {

    private static final String AI_BASE = "http://finhub-ai:8087";

    // 포워딩 시 제외할 헤더 (hop-by-hop + host)
    private static final Set<String> SKIP_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding", "connection",
            "keep-alive", "te", "trailers", "upgrade"
    );

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping("/api/v1/ai/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        // 1. 대상 URL 구성
        String url = AI_BASE + request.getRequestURI();
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        // 2. 요청 바디 읽기 (GET 등 바디 없는 경우 빈 배열)
        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        // 3. 헤더 복사 (hop-by-hop 제외, Content-Length 제외하여 RestTemplate이 직접 계산)
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !SKIP_HEADERS.contains(name.toLowerCase()))
                .forEach(name -> headers.add(name, request.getHeader(name)));

        // 4. 바디가 있을 때만 HttpEntity에 포함
        HttpEntity<byte[]> entity = body.length > 0
                ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(headers);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        log.debug("AI 프록시: {} {} → {}", method, request.getRequestURI(), url);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(URI.create(url), method, entity, byte[].class);

            // 5. 응답 헤더에서 CORS 제거 (gateway CorsFilter가 추가하므로 중복 방지)
            HttpHeaders respHeaders = new HttpHeaders();
            resp.getHeaders().forEach((name, values) -> {
                if (!name.toLowerCase().startsWith("access-control-")) {
                    respHeaders.addAll(name, values);
                }
            });

            return ResponseEntity.status(resp.getStatusCode())
                    .headers(respHeaders)
                    .body(resp.getBody());

        } catch (HttpStatusCodeException e) {
            log.warn("AI 서비스 오류: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        }
    }
}
