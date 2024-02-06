package com.shopping.ekart.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.User;

public interface UserRepositary extends JpaRepository<User, Integer>{
	
	boolean existsByEmail(String email);

}
