package com.shopping.ekart.repositary;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.AccessToken;

public interface AccessTokenRepositary extends JpaRepository<AccessToken, Long>{

	Optional<AccessToken> findByToken(String at);

}
