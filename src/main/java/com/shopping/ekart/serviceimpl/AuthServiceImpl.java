package com.shopping.ekart.serviceimpl;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopping.ekart.entity.Customer;
import com.shopping.ekart.entity.Seller;
import com.shopping.ekart.entity.User;
import com.shopping.ekart.enums.USERROLE;
import com.shopping.ekart.exceptions.IllegalRequestException;
import com.shopping.ekart.exceptions.UserAlreadyExistByEmailException;
import com.shopping.ekart.repositary.CustomerRepositary;
import com.shopping.ekart.repositary.SellerRepositary;
import com.shopping.ekart.repositary.UserRepositary;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.ResponseStructure;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

	private UserRepositary userRepo;

	private SellerRepositary sellerRepo;

	private CustomerRepositary customerRepo;

	private ResponseStructure<UserResponse> structure;
	
	private PasswordEncoder passwordEncoder;

	private <T extends User>T mapToUser(UserRequest userRequest)
	{
		User user=null;

		switch (userRequest.getUserRole()) {
		case "CUSTOMER"->user=new Customer();

		case "SELLER"->user=new Seller();

		default->throw new IllegalRequestException("Invalid User Role..!!! "+userRequest.getUserRole());

		}
		String userName=userRequest.getEmail();

		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(passwordEncoder.encode(userRequest));
		user.setUserRole(USERROLE.valueOf(userRequest.getUserRole()));
		

		return (T) user;

	}

	private UserResponse mapToUserResponse(User user){
		return  new UserResponse().builder()
				.userName(user.getUserName())
				.userId(user.getUserId())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.isDeleted(user.isDeleted())
				.isEmailVerified(user.isEmailVerified())
				.build();
	}

	private User saveUser(UserRequest  userRequest)
	{
		User user = mapToUser(userRequest);
		switch (user.getUserRole()) {
		case CUSTOMER->user=customerRepo.save((Customer)user);

		case SELLER->user=sellerRepo.save((Seller)user);

		default->throw new IllegalRequestException("Invalid User Role...!!! "+user.getUserRole());

		}
		return  user;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

	     User user = userRepo.findByUserName(userRequest.getEmail().split("@")[0]).map(u->{
			
			if(u.isEmailVerified()) throw new UserAlreadyExistByEmailException("Registration Failed, User with this email already existts");
			
			else {
				// Send otp to email
			}
			return u;
		}).orElseGet(()->saveUser(userRequest));
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure.setStatusCode(HttpStatus.OK.value())
				.setMessage("Please Varify your email by OTP sent to your email")
				.setData(mapToUserResponse(user)),HttpStatus.OK);
		
	}

	@Override
	public void cleanUpNonVerifiedUsers() {
		
		List<User> list = userRepo.findByIsEmailVerifiedFalse();
		
		if(!list.isEmpty())
		{
			list.forEach(user->userRepo.delete(user));
		}		
		
	}

}
