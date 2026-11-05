package com.veth007.hotelmanagement.service.Discount.Implement;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.DiscountEntity;
import com.veth007.hotelmanagement.model.response.DiscountResponse;
import com.veth007.hotelmanagement.repository.DiscountRepository;
import com.veth007.hotelmanagement.service.Discount.DiscountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountServiceImp implements DiscountService {
    private final DiscountRepository discountRepository;

    @Override
    public ApiResponse<List<DiscountResponse>> get_all(int page, int size, boolean isPage){
        try {
            List<DiscountEntity> discountEntity;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
                discountEntity = this.discountRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
                discountEntity = this.discountRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(discountEntity.stream().map(DiscountResponse::fromEntity).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve discounts: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<DiscountResponse> get_by_id(Long id){
        DiscountEntity discountEntity = this.discountRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Discount not found with id: " + id));   try {
         
            return ApiResponse.success(DiscountResponse.fromEntity(discountEntity));
        }catch (Exception e){
            throw new RuntimeException("Failed to retrieve discount: " + e.getMessage(), e);
        }
    }
}
