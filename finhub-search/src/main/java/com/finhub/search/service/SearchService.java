package com.finhub.search.service;

import com.finhub.search.dto.response.SearchResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    Page<SearchResultResponse> search(String keyword, String category, Pageable pageable);
}
