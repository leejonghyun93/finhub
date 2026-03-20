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
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
public class ProxyController {

    private static final Map<String, String> ROUTE_MAP = Map.of(
            "/api/v1/users",        "http://finhub-user:8081",
            "/api/v1/banking",      "http://finhub-banking:8082",
            "/api/v1/investment",   "http://finhub-investment:8083",
            "/api/v1/payment",      "http://finhub-payment:8084",
            "/api/v1/insurance",    "http://finhub-insurance:8085",
            "/api/v1/search",       "http://finhub-search:8086",
            "/api/v1/notification", "http://finhub-notification:8088"
    );

    private static final Set<String> SKIP_HEADERS = Set.of(
            "host", "content-length", "transfer-encoding", "connection",
            "keep-alive", "te", "trailers", "upgrade"
    );

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping({
            "/api/v1/users/**",
            "/api/v1/banking/**",
            "/api/v1/investment/**",
            "/api/v1/payment/**",
            "/api/v1/insurance/**",
            "/api/v1/search/**",
            "/api/v1/notification/**"
    })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String requestUri = request.getRequestURI();
        String targetBase = resolveTarget(requestUri);
        if (targetBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String url = targetBase + requestUri;
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).stream()
                .filter(name -> !SKIP_HEADERS.contains(name.toLowerCase()))
                .forEach(name -> headers.add(name, request.getHeader(name)));

        HttpEntity<byte[]> entity = body.length > 0
                ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(headers);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        log.debug("프록시: {} {} → {}", method, requestUri, url);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(URI.create(url), method, entity, byte[].class);

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
            log.warn("서비스 오류: {} {} - {}", method, url, e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    private String resolveTarget(String uri) {
        return ROUTE_MAP.entrySet().stream()
                .filter(e -> uri.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
