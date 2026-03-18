package com.finhub.search.dto.response;

import com.finhub.search.document.FinancialProductDocument;

public record SearchResultResponse(
        String id,
        String type,
        String name,
        String description,
        String extraInfo
) {
    public static SearchResultResponse from(FinancialProductDocument doc) {
        return new SearchResultResponse(
                doc.getId(),
                doc.getType(),
                doc.getName(),
                doc.getDescription(),
                doc.getExtraInfo()
        );
    }
}
