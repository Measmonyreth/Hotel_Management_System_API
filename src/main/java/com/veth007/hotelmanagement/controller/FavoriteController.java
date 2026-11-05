package com.veth007.hotelmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.request.Favorite.FavoriteRequest;
import com.veth007.hotelmanagement.model.response.Favorite.FavoriteResponse;
import com.veth007.hotelmanagement.service.Favorite.FavoriteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PostMapping("/favourite")
    public ResponseEntity<ApiResponse<FavoriteResponse>> favourite(@AuthenticationPrincipal CustomerDetails userDetails, @Valid @RequestBody FavoriteRequest request) {
        return ResponseEntity.ok(this.favoriteService.favourite(request, userDetails.getId()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('CUSTOMER'))")
    @PostMapping("/unfavourite")
    public ResponseEntity<ApiResponse<FavoriteResponse>> unfavourite(@AuthenticationPrincipal CustomerDetails userDetails, @Valid @RequestBody FavoriteRequest request) {
        return ResponseEntity.ok(this.favoriteService.unfavourite(request, userDetails.getId()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getAllFavorite(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        return ResponseEntity.ok(this.favoriteService.get_all(page, size, isPage));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> getFavoriteById(@PathVariable String id) {
        return ResponseEntity.ok(this.favoriteService.get_by_id(HashIdUtil.resolveId(id)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('CUSTOMER'))")
    @GetMapping("/getallbyuser/{customerId}")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getAllFavoriteByUser(@PathVariable String customerId) {
        return ResponseEntity.ok(this.favoriteService.get_all_by_user(HashIdUtil.resolveId(customerId)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('CUSTOMER'))")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(@AuthenticationPrincipal CustomerDetails userDetails) {
        return ResponseEntity.ok(this.favoriteService.get_all_by_user(userDetails.getId()));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> updateFavorite(@PathVariable String id,
            @RequestParam String status) {
        return ResponseEntity.ok(this.favoriteService.update(HashIdUtil.resolveId(id), status));
    }
}
