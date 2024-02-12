package com.shopping.ekart.responsedto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	
	private int userId;
	private String userName;
	private String userRole;
	private boolean isAuthenticated;
	private LocalDateTime accessExpiration;
	private LocalDateTime refereshExpiration;

}
