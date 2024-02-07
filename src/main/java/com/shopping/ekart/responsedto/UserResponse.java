package com.shopping.ekart.responsedto;

import com.shopping.ekart.enums.USERROLE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

	private int userId;
	private String userName;
	private String email;
	private USERROLE userRole;
	
	boolean isEmailVerified;
	boolean isDeleted;
}
