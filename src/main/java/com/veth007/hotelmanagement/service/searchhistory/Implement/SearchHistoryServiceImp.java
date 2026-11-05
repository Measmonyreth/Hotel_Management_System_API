package com.veth007.hotelmanagement.service.searchhistory.Implement;

import java.util.List;
import org.springframework.stereotype.Service;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.SearchHistoryEntity;
import com.veth007.hotelmanagement.model.request.searchhistory.SearchHistoryRequest;
import com.veth007.hotelmanagement.model.response.searchhistory.SearchHistoryResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.SearchHistoryRepository;
import com.veth007.hotelmanagement.service.searchhistory.SearchHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImp implements SearchHistoryService {

    private final SearchHistoryRepository repo;
    private final CustomerRepository customerRepo;

    // record a new search keyword for a customer
    public ApiResponse<SearchHistoryResponse> record(SearchHistoryRequest request, Long customer_id) {
        CustomerEntity customer = customerRepo.findById(customer_id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));
        SearchHistoryEntity saved = repo.save(SearchHistoryEntity.builder()
                .customer(customer)
                .keyword(request.getKeyword())
                .build());
        return ApiResponse.success(SearchHistoryResponse.fromEntity(saved));
    }

    // personal search history (most recent first)
    public ApiResponse<List<SearchHistoryResponse>> history(Long customerId) {
        List<SearchHistoryEntity> histories = repo.findByCustomer_IdOrderByCreatedDateDesc(customerId);
        return ApiResponse.success(histories.stream().map(SearchHistoryResponse::fromEntity).toList());
    }

    public ApiResponse<Long> count(Long customerId) {
        return ApiResponse.success(repo.countByCustomer_Id(customerId));
    }

    // delete a single search history entry
    public ApiResponse<String> delete(Long id) {
        repo.deleteById(id);
        return ApiResponse.ok("Search history deleted");
    }
}
