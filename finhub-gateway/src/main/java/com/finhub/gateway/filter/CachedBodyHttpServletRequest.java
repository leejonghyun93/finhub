package com.finhub.gateway.filter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.util.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    // 모든 키를 소문자로 저장 (HTTP 헤더는 대소문자 구분 없음)
    private final Map<String, String> customHeaders = new HashMap<>();

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        // Content-Length를 캐싱된 body 크기로 명시 설정
        // → 게이트웨이가 "Content-Length: N" 단독 사용, Transfer-Encoding: chunked 미사용
        customHeaders.put("content-length", String.valueOf(cachedBody.length));
    }

    public void putHeader(String name, String value) {
        customHeaders.put(name.toLowerCase(), value);
    }

    // ── Body ────────────────────────────────────────────────

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    // ── Headers ─────────────────────────────────────────────

    @Override
    public String getHeader(String name) {
        if (name == null) return null;
        // host는 게이트웨이가 target 주소로 새로 설정 → 원본 전달 시 중복
        if ("host".equalsIgnoreCase(name)) return null;
        String value = customHeaders.get(name.toLowerCase());
        return value != null ? value : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (name == null) return Collections.emptyEnumeration();
        if ("host".equalsIgnoreCase(name)) return Collections.emptyEnumeration();
        String value = customHeaders.get(name.toLowerCase());
        if (value != null) {
            return Collections.enumeration(List.of(value));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(customHeaders.keySet());
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            String headerName = original.nextElement().toLowerCase();
            if (!"host".equals(headerName) && !customHeaders.containsKey(headerName)) {
                names.add(headerName);
            }
        }
        return Collections.enumeration(names);
    }
}
