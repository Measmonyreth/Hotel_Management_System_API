package com.veth007.hotelmanagement.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.FavoriteStatus;
import com.veth007.hotelmanagement.model.entity.FavoritesEntity;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoritesEntity, Long>, JpaSpecificationExecutor<FavoritesEntity> {
    List<FavoritesEntity> findByCustomer_Id(Long customerId);
    List<FavoritesEntity> findByRoom_Id(Long roomId);
    FavoritesEntity findByCustomer_IdAndRoom_Id(Long customerId, Long roomId);
    boolean existsByCustomer_IdAndRoom_Id(Long customerId, Long roomId);

    boolean existsByIdAndDeletedDateIsNotNull(Long id);
    FavoritesEntity findByIdAndDeletedDateIsNotNull(Long id);
    boolean existsByDeletedDateIsNotNull();
    boolean existsByDeletedByIsNotNull();
    List<FavoritesEntity> findByDeletedByIsNotNull();
    List<FavoritesEntity> findByDeletedDateIsNotNull();

    int countByFavoriteStatus(FavoriteStatus favoriteStatus);
}
