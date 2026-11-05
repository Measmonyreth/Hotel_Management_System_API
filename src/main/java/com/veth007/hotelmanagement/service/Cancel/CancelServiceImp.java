package com.veth007.hotelmanagement.service.Cancel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.PaymentTiming;
import com.veth007.hotelmanagement.constant.enums.RefundStatus;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.CancelRequestEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.request.Cancel.CancelRequest;
import com.veth007.hotelmanagement.model.response.Cancel.CancelResponse;
import com.veth007.hotelmanagement.repository.BookRepository;
import com.veth007.hotelmanagement.repository.CancelRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CancelServiceImp implements CancelService {
    private final CancelRepository cancelRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public ApiResponse<CancelResponse> createCancellationRequest(Long bookId, String reason) {
        // 1. Fetch and validate the Booking
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookId));

        if (cancelRepository.existsByBookId(bookId)) {
            throw new AlreadyExistException("This booking had requesed");
        }

        if (book.getBookStatus() == BookStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        CancelRequestEntity request = CancelRequestEntity.builder()
                .book(book)
                .reason(reason)
                .calculatedPenaltyFee(BigDecimal.ZERO)
                .calculatedRefundAmount(BigDecimal.ZERO)
                .timing(null)
                .status(RefundStatus.REQUESTED) // Explicitly set to review state
                .reviewedDate(LocalDateTime.now())
                .build();
        if(request != null){
            book.setBookStatus(BookStatus.CANCELLED);
            bookRepository.save(book);
        }

        return ApiResponse.success(CancelResponse.fromEntity(cancelRepository.save(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CancelResponse>> getAllRequests() {
        List<CancelRequestEntity> requests = cancelRepository.findAll();
        List<CancelResponse> responses = requests.stream().map(CancelResponse::fromEntity).toList();
        return ApiResponse.success(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CancelResponse> getRequestById(Long id) {
        CancelRequestEntity request = cancelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cancellation request not found with ID: " + id));
        return ApiResponse.success(CancelResponse.fromEntity(request));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CancelResponse>> getCustomerRequests(Long customerId) {
        List<CancelResponse> requests = cancelRepository
                .findByBook_Customer_IdAndDeletedDateIsNull(customerId)
                .stream()
                .map(CancelResponse::fromEntity)
                .toList();
        return ApiResponse.success(requests);
    }

    @Override
    @Transactional
    public ApiResponse<CancelResponse> updateRequest(Long id, CancelRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cancellation request body is required");
        }

        CancelRequestEntity existingRequest = cancelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cancellation request not found with ID: " + id));

        if (request.getReason() != null) {
            if (request.getReason().isBlank()) {
                throw new IllegalArgumentException("Cancellation reason cannot be blank");
            }
            existingRequest.setReason(request.getReason().trim());
        }
        if (request.getStatus() != null) {
            existingRequest.setStatus(RefundStatus.valueOf(request.getStatus()));
            existingRequest.setReviewedBy(
                    SecurityContextHolder.getContext().getAuthentication().getName());
            existingRequest.setReviewedDate(LocalDateTime.now());
        }
        if (request.getTiming() != null) {
            existingRequest.setTiming(PaymentTiming.valueOf(request.getTiming()));
        }
        if (request.getCalculatedPenaltyFee() != null) {
            existingRequest.setCalculatedPenaltyFee(
                    BigDecimal.valueOf(request.getCalculatedPenaltyFee()));
        }
        if (request.getCalculatedRefundAmount() != null) {
            existingRequest.setCalculatedRefundAmount(
                    BigDecimal.valueOf(request.getCalculatedRefundAmount()));
        }
        if (request.getRejectionReason() != null) {
            existingRequest.setRejectionReason(request.getRejectionReason());
        }

        CancelRequestEntity updatedRequest = cancelRepository.save(existingRequest);
        return ApiResponse.success(CancelResponse.fromEntity(updatedRequest));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteRequest(Long id) {
        CancelRequestEntity request = cancelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cancellation request not found with ID: " + id));
        if (request.getDeletedDate() != null) {
            throw new NotFoundException("Cancellation request is already deleted with ID: " + id);
        }

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        request.setDeletedDate(LocalDateTime.now());
        request.setDeletedBy(userName);
        cancelRepository.save(request);

        return ApiResponse.success("Cancellation request deleted successfully");
    }

    @Override
    public ApiResponse<String> updateStatus(Long id, CancelRequest request) {
        CancelRequestEntity existingRequest = cancelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cancellation request not found with ID: " + id));

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        // Update fields from the request
        existingRequest.setStatus(RefundStatus.valueOf(request.getStatus()));
        existingRequest.setReviewedBy(user);
        existingRequest.setRejectionReason(request.getRejectionReason());
        existingRequest.setReviewedDate(LocalDateTime.now());

        cancelRepository.save(existingRequest);
        return ApiResponse.success("Cancellation request updated successfully");
    }

}
