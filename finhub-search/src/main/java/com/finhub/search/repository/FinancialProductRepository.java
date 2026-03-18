package com.finhub.search.repository;

import com.finhub.search.document.FinancialProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FinancialProductRepository extends ElasticsearchRepository<FinancialProductDocument, String> {

    Page<FinancialProductDocument> findByNameContainingOrDescriptionContaining(
            String name, String description, Pageable pageable);

    Page<FinancialProductDocument> findByTypeAndNameContainingOrTypeAndDescriptionContaining(
            String type1, String name, String type2, String description, Pageable pageable);

    Page<FinancialProductDocument> findByType(String type, Pageable pageable);
}
