package com.shopping.ekart.repositary;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.RefreshToken;

public interface RefreshTokenRepositary extends JpaRepository<RefreshToken, Long>{

	Optional<RefreshToken> findByToken(String rt);

}
