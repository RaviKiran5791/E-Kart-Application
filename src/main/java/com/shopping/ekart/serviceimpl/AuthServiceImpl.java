package com.shopping.ekart.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shopping.ekart.entity.User;
import com.shopping.ekart.enums.USERROLE;
import com.shopping.ekart.repositary.CustomerRepositary;
import com.shopping.ekart.repositary.SellerRepositary;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.ResponseStructure;

@Service
public class AuthServiceImpl implements AuthService{
	
	@Autowired
	private SellerRepositary sellerRepo;
	
	@Autowired
	private CustomerRepositary coCustomerRepo;
	
	
	private <T extends User>T mapToUser(UserRequest userRequest){
		return (T) new User().builder()
				.email(userRequest.getEmail())
				.password(userRequest.getPassword())
				.userRole(USERROLE.valueOf(userRequest.getUserRole()))
				.build();
	}
	private <T extends User>T mapToUserResponse(User user){
		return (T) new User().builder()
				.userId(user.getUserId())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		
		
		
		return null;
	}

}
