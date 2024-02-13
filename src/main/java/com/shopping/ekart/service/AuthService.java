package com.shopping.ekart.service;

import org.springframework.http.ResponseEntity;

import com.shopping.ekart.requestdto.AuthRequest;
import com.shopping.ekart.requestdto.OtpModel;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.AuthResponse;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.util.ResponseStructure;
import com.shopping.ekart.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

	void cleanUpNonVerifiedUsers();


	ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response, String accessToken, String refreshToken);

	ResponseEntity<SimpleResponseStructure> logut(String accessToken, String refreshToken, HttpServletResponse httpServletResponse);
	
	void cleanUpExpiredAccessToken();
	void cleanUpExpiredRefereshToken();

	ResponseEntity<SimpleResponseStructure> revokeOther(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleResponseStructure> revokeAll(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleResponseStructure> refreshLoginAndTokenRotation(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

}
