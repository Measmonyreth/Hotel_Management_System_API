package com.veth007.hotelmanagement.service.Favorite;

import java.util.List;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Favorite.FavoriteRequest;
import com.veth007.hotelmanagement.model.response.Favorite.FavoriteResponse;

public interface FavoriteService {

    ApiResponse<FavoriteResponse> favourite(FavoriteRequest request, Long customerId);

    ApiResponse<FavoriteResponse> unfavourite(FavoriteRequest request, Long customerId);

    ApiResponse<List<FavoriteResponse>> get_all(int page, int size, boolean isPage);

    ApiResponse<FavoriteResponse> get_by_id(Long id);

    ApiResponse<List<FavoriteResponse>> get_all_by_user(Long customerId);

    ApiResponse<FavoriteResponse> update(Long id, String status);
}
