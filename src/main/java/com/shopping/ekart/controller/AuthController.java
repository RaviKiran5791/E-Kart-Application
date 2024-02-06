package com.shopping.ekart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.ResponseStructure;

@Controller
public class AuthController {
	
	@Autowired
	private AuthService authService;
	
	@PostMapping("/users/register")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody UserRequest userRequest)
	{
		return authService.registerUser(userRequest);
	}

}
