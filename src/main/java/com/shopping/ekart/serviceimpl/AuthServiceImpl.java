package com.shopping.ekart.serviceimpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.shopping.ekart.entity.Customer;
import com.shopping.ekart.entity.Seller;
import com.shopping.ekart.entity.User;
import com.shopping.ekart.enums.USERROLE;
import com.shopping.ekart.exceptions.DataAlreadyExist;
import com.shopping.ekart.exceptions.IllegalRequestException;
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

	private <T extends User>T mapToUser(UserRequest userRequest)
	{
		User user=null;

		switch (userRequest.getUserRole()) {
		case "CUSTOMER"->user=new Customer();

		case "SELLER"->user=new Seller();

		default->throw new IllegalRequestException("Invalid User Role..!!!");

		}
		String userName=userRequest.getEmail();

		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		user.setUserRole(USERROLE.valueOf(userRequest.getUserRole()));
		

		return (T) user;

	}

	private UserResponse mapToUserResponse(User user){
		return  new UserResponse().builder()
				.userId(user.getUserId())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();
	}

	private User saveUser(User user)
	{
		switch (user.getUserRole()) {
		case CUSTOMER->user=customerRepo.save((Customer)user);

		case SELLER->user=sellerRepo.save((Seller)user);

		default->throw new IllegalRequestException("Invalid User Role...!!!!");

		}
		return  user;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

		boolean existsByEmail = userRepo.existsByEmail(userRequest.getEmail());

		if(!existsByEmail)
		{
			User user = mapToUser(userRequest);
			user=saveUser(user);
			UserResponse userResponse = mapToUserResponse(user);
			return new ResponseEntity<ResponseStructure<UserResponse>>(structure.setStatusCode(HttpStatus.OK.value())
					.setMessage("User registred Successfully,Please Varify your email by OTP")
					.setData(userResponse),HttpStatus.OK);
		}
		throw new DataAlreadyExist("User Already Present!!!");
	}

}
