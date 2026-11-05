package com.veth007.hotelmanagement.service.Favorite.Implement;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.veth007.hotelmanagement.constant.enums.FavoriteStatus;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.FavoritesEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.request.Favorite.FavoriteRequest;
import com.veth007.hotelmanagement.model.response.Favorite.FavoriteResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.FavoriteRepository;
import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.service.Favorite.FavoriteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImp implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;

    // ─── Favourite a room (create new or re-activate existing) ───
    @Override
    public ApiResponse<FavoriteResponse> favourite(FavoriteRequest request, Long customerId) {
        try {
            CustomerEntity customer = this.customerRepository.findById(customerId)
                    .orElseThrow(() -> new NotFoundException(
                            "Customer not found with id: " + customerId));
            RoomEntity room = this.roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new NotFoundException(
                            "Room not found with id: " + request.getRoomId()));

            FavoritesEntity favorite = this.favoriteRepository
                    .findByCustomer_IdAndRoom_Id(customer.getId(), room.getId());

            if (favorite == null) {
                favorite = FavoritesEntity.builder()
                        .customer(customer)
                        .room(room)
                        .favoriteStatus(FavoriteStatus.ACTIVE)
                        .build();
            } else {
                favorite.setFavoriteStatus(FavoriteStatus.ACTIVE);
            }

            return ApiResponse.success(FavoriteResponse.fromEntity(this.favoriteRepository.save(favorite)));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to favourite room: " + e.getMessage(), e);
        }
    }

    // ─── Unfavourite a room (set status INACTIVE) ───
    @Override
    public ApiResponse<FavoriteResponse> unfavourite(FavoriteRequest request, Long customerId) {
        try {
            FavoritesEntity favorite = this.favoriteRepository
                    .findByCustomer_IdAndRoom_Id(customerId, request.getRoomId());

            if (favorite == null) {
                throw new NotFoundException("Favorite not found for customer id: "
                        + customerId + " and room id: " + request.getRoomId());
            }

            favorite.setFavoriteStatus(FavoriteStatus.INACTIVE);
            return ApiResponse.success(FavoriteResponse.fromEntity(this.favoriteRepository.save(favorite)));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to unfavourite room: " + e.getMessage(), e);
        }
    }

    // ─── List all favorites ───
    @Override
    public ApiResponse<List<FavoriteResponse>> get_all(int page, int size, boolean isPage) {
        try {
            List<FavoritesEntity> favorites;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
                favorites = this.favoriteRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
                favorites = this.favoriteRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(favorites.stream().map(FavoriteResponse::fromEntity).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all favorites: " + e.getMessage(), e);
        }
    }

    // ─── Get a favorite by id ───
    @Override
    public ApiResponse<FavoriteResponse> get_by_id(Long id) {
        try {
            FavoritesEntity favorite = this.favoriteRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Favorite not found with id: " + id));
            return ApiResponse.success(FavoriteResponse.fromEntity(favorite));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get favorite: " + e.getMessage(), e);
        }
    }

    // ─── List all favorites of a user ───
    @Override
    public ApiResponse<List<FavoriteResponse>> get_all_by_user(Long customerId) {
        try {
            if (!this.customerRepository.existsById(customerId)) {
                throw new NotFoundException("Customer not found with id: " + customerId);
            }
            List<FavoritesEntity> favorites = this.favoriteRepository.findByCustomer_Id(customerId);
            return ApiResponse.success(favorites.stream().map(FavoriteResponse::fromEntity).toList());
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get favorites of customer: " + e.getMessage(), e);
        }
    }

    // ─── Update a favorite's status ───
    @Override
    public ApiResponse<FavoriteResponse> update(Long id, String status) {
        try {
            FavoritesEntity favorite = this.favoriteRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Favorite not found with id: " + id));

            if (status != null) {
                favorite.setFavoriteStatus(FavoriteStatus.valueOf(status.toUpperCase()));
            }

            return ApiResponse.success(FavoriteResponse.fromEntity(this.favoriteRepository.save(favorite)));
        } catch (NotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid favorite status: " + status, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update favorite: " + e.getMessage(), e);
        }
    }
}
