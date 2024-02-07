package com.shopping.ekart.repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.ekart.entity.Customer;

public interface CustomerRepositary extends JpaRepository<Customer, Integer>{

}
