package com.veth007.hotelmanagement.model.response.Favorite;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.FavoritesEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse implements Serializable {
    private Long id;
    private Long customer_id;
    private Long room_id;
    private String favorite_status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    public static FavoriteResponse fromEntity(FavoritesEntity favorite) {
        return FavoriteResponse.builder()
        .id(favorite.getId())
        .customer_id(favorite.getCustomer() != null ? favorite.getCustomer().getId() : null)
        .room_id(favorite.getRoom() != null ? favorite.getRoom().getId() : null)
        .favorite_status(favorite.getFavoriteStatus().name())
        .createdDate(favorite.getCreatedDate())
        .lastModifiedDate(favorite.getLastModifiedDate())
        .deletedDate(favorite.getDeletedDate())
        .createBy(favorite.getCreatedBy())
        .lastModifiedBy(favorite.getLastModifiedBy())
        .deletedBy(favorite.getDeleted_by())
        .build();
    }
}
