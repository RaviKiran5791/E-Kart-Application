package com.shopping.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopping.ekart.requestdto.AuthRequest;
import com.shopping.ekart.requestdto.OtpModel;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.AuthResponse;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.ResponseStructure;
import com.shopping.ekart.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	
	@PostMapping("/verifyotp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(@RequestBody OtpModel otpModel)
	{
		return authService.verifyOtp(otpModel);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest, HttpServletResponse response){
		return authService.login(authRequest,response);
	}
//	@PostMapping("/logout")
//	public ResponseEntity<ResponseStructure<AuthResponse>> logut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
//		return authService.logut(httpServletRequest,httpServletResponse);
//	}
	
	// or traditional approach
	
	
	@PostMapping("/logout")
	public ResponseEntity<SimpleResponseStructure> logut(@CookieValue(name = "at",required = false)String accessToken,@CookieValue(name="rt",required = false)String refreshToken ,HttpServletResponse httpServletResponse){
		return authService.logut(accessToken,refreshToken,httpServletResponse);
	}
	
	@PostMapping("/revoke-other-devices")
	public ResponseEntity<SimpleResponseStructure> revokeOther(@CookieValue(name = "at",required = false)String accessToken,@CookieValue(name="rt",required = false)String refreshToken ,HttpServletResponse httpServletResponse){
		return authService.revokeOther(accessToken,refreshToken,httpServletResponse);
	}
	@PostMapping("/revoke-all-devices")
	public ResponseEntity<SimpleResponseStructure> revokeAll(@CookieValue(name = "at",required = false)String accessToken,@CookieValue(name="rt",required = false)String refreshToken ,HttpServletResponse httpServletResponse){
		return authService.revokeAll(accessToken,refreshToken,httpServletResponse);
	}
	@PostMapping("/refresh-token")
	public ResponseEntity<SimpleResponseStructure> refreshLoginAndTokenRotation(@CookieValue(name = "at",required = false)String accessToken,@CookieValue(name="rt",required = false)String refreshToken ,HttpServletResponse httpServletResponse){
		return authService.refreshLoginAndTokenRotation(accessToken,refreshToken,httpServletResponse);
	}
	

}
