package com.finhub.search.service;

import com.finhub.search.document.FinancialProductDocument;
import com.finhub.search.dto.response.SearchResultResponse;
import com.finhub.search.repository.FinancialProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FinancialProductRepository repository;

    @Override
    public Page<SearchResultResponse> search(String keyword, String category, Pageable pageable) {
        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasCategory = StringUtils.hasText(category);

        Page<FinancialProductDocument> results;

        if (hasKeyword && hasCategory) {
            results = repository.findByTypeAndNameContainingOrTypeAndDescriptionContaining(
                    category.toUpperCase(), keyword,
                    category.toUpperCase(), keyword,
                    pageable);
        } else if (hasKeyword) {
            results = repository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
        } else if (hasCategory) {
            results = repository.findByType(category.toUpperCase(), pageable);
        } else {
            results = repository.findAll(pageable);
        }

        return results.map(SearchResultResponse::from);
    }
}
