package com.veth007.hotelmanagement.repository;

import com.veth007.hotelmanagement.model.entity.FcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {
    
    Optional<FcmTokenEntity> findByToken(String token);
    
    List<FcmTokenEntity> findByCustomer_Id(Long userId);

    // find last active token for a user
    Optional<FcmTokenEntity> findFirstByCustomer_IdAndIsActiveTrueOrderByIdDesc(Long userId);
    
    List<FcmTokenEntity> findByCustomer_IdAndIsActiveTrue(Long userId);
    
    @Query("SELECT f.token FROM FcmTokenEntity f WHERE f.customer.id = :userId AND f.isActive = true")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);
    
    void deleteByToken(String token);
    
    @Query("DELETE FROM FcmTokenEntity f WHERE f.customer.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
 