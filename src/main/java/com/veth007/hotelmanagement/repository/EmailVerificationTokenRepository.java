package com.veth007.hotelmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.EmailVerificationToken;


@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long>{
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByEmail(String email);
    void deleteByEmail(String email);

    Enum<EmailVerificationToken.TokenStatus> findStatusByEmail(String email);
    Optional<EmailVerificationToken> findByEmailAndToken(String email, String token);
}
