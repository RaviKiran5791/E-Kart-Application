package com.shopping.ekart.service;

import org.springframework.http.ResponseEntity;

import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.util.ResponseStructure;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

}
