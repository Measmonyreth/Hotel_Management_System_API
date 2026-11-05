package com.veth007.hotelmanagement.model.response.searchhistory;

import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.SearchHistoryEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponse {
    private Long id;
    private Long customerId;
    private String keyword;
    private LocalDateTime createdDate;

    public static SearchHistoryResponse fromEntity(SearchHistoryEntity entity) {
        SearchHistoryResponse response = new SearchHistoryResponse();
        response.setId(entity.getId());
        response.setCustomerId(entity.getCustomer() != null ?entity.getCustomer().getId() : null);
        response.setKeyword(entity.getKeyword());
        response.setCreatedDate(entity.getCreatedDate());
        return response;
    }
}
