package com.shopping.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.ResponseStructure;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

	private AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody @Valid UserRequest userRequest) {
		return authService.registerUser(userRequest);
	}

}
