package com.shopping.ekart.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.Seller;

public interface SellerRepositary extends JpaRepository<Seller, Integer>{

}
