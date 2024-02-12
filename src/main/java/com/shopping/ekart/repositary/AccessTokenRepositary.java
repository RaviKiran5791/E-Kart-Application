package com.shopping.ekart.repositary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.AccessToken;

public interface AccessTokenRepositary extends JpaRepository<AccessToken, Long>{

	Optional<AccessToken> findByToken(String at);
	
	List<AccessToken> findAllByExpirationBefore(LocalDateTime currentTime);

	Optional<AccessToken> findByTokenAndIsBlocked(String token, boolean isBlocked);

}
