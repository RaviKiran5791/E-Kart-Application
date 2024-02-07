package com.shopping.ekart.repositary;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.User;

public interface UserRepositary extends JpaRepository<User, Integer>{
	

	Optional<User> findByUserName(String userName);


	Optional<User> findByUserName(String username);

}
