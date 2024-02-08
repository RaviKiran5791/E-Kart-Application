package com.shopping.ekart.serviceimpl;


import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopping.ekart.cache.CacheStore;
import com.shopping.ekart.entity.Customer;
import com.shopping.ekart.entity.Seller;
import com.shopping.ekart.entity.User;
import com.shopping.ekart.enums.USERROLE;
import com.shopping.ekart.exceptions.IllegalRequestException;
import com.shopping.ekart.exceptions.UserAlreadyExistByEmailException;
import com.shopping.ekart.repositary.CustomerRepositary;
import com.shopping.ekart.repositary.SellerRepositary;
import com.shopping.ekart.repositary.UserRepositary;
import com.shopping.ekart.requestdto.OtpModel;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.MessageStructure;
import com.shopping.ekart.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

	private UserRepositary userRepo;

	private SellerRepositary sellerRepo;

	private CustomerRepositary customerRepo;

	private ResponseStructure<UserResponse> structure;

	private PasswordEncoder passwordEncoder;

	private CacheStore<String> otpCacheStore;
	
	private CacheStore<User> userCacheStore;
	
	private JavaMailSender javaMailSender; // automatially bean created by spring boot mail

	private <T extends User>T mapToChildUser(UserRequest userRequest)
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
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
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
		User user = mapToChildUser(userRequest);
		switch (user.getUserRole()) {
		case CUSTOMER->user=customerRepo.save((Customer)user);

		case SELLER->user=sellerRepo.save((Seller)user);

		default->throw new IllegalRequestException("Invalid User Role...!!! "+user.getUserRole());

		}
		return  user;
	}
	@Async
	private void sentMail(MessageStructure message) throws MessagingException 
	{
		MimeMessage mimeMessage=javaMailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText());
		javaMailSender.send(mimeMessage);
		
	}
	private void sendOtpToMail(User user,String otp) throws MessagingException
	{
		
		sentMail(MessageStructure.builder()
		         .to(user.getEmail())
		         .subject("Complete Your Registration To E-Kart")
		         .sentDate(new Date())
		         .text("het,"+user.getUserName()+"Good to see you intrested in E-Kart"
		          +"Complete your registration process using the otp <br>"
				  +"<h1>"+otp+"</h1><br>"
				  +"Note : OTP expires in 1 minute"
				  +"<br><br>"
				  +"with best regards <br>"
				  + "E-Kart")
		           .build());
	}
	private void sendRegistrationSuccessfullMail(User user) throws MessagingException
	{
		
		sentMail(MessageStructure.builder()
		         .to(user.getEmail())
		         .subject("Complete Your Registration To E-Kart")
		         .sentDate(new Date())
		         .text("Wellcome "+user.getUserName()
		          +"your registration Successfully completed<br>"
		          +"Now you can buy products"
				  +"<br><br>"
				  +"with best regards <br>"
				  + "E-Kart")
		           .build());
	}
	
	private String generateOTP() {
		return String.valueOf(new Random().nextInt(100000,999999));
    }

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

		if(userRepo.existsByEmail(userRequest.getEmail()))throw new UserAlreadyExistByEmailException("User Already Present For given Email Id");	

		String OTP=generateOTP();
		User user=mapToChildUser(userRequest);
		
		userCacheStore.add(userRequest.getEmail(), user);
		otpCacheStore.add(userRequest.getEmail(), OTP);
		
		
		try {
			sendOtpToMail(user, OTP);
		} catch (MessagingException e) {
			
			log.error("The email Address dosen't exist");
		}
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure.setStatusCode(HttpStatus.OK.value())
				.setMessage("Please Varify through OTP Sent your email id ")
				.setData(mapToUserResponse(user)),HttpStatus.OK);

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(OtpModel otpModel) {
		User user=userCacheStore.get(otpModel.getEmail());
		String otp=otpCacheStore.get(otpModel.getEmail());
		
		if(otp==null) throw new IllegalRequestException("OTP expired");
		if(user==null) throw new IllegalRequestException("User Session Expired");
		if(!otp.equals(otpModel.getOtp())) throw new IllegalRequestException("Invalid OTP exception, plese re-enter proper OTP");
		
		user.setEmailVerified(true);
		
		userRepo.save(user);
		try {
			sendRegistrationSuccessfullMail(user);
		} catch (MessagingException e) {
			log.error("Connection Error");
		}
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure.setStatusCode(HttpStatus.OK.value())
				.setMessage("User Registred Successfully")
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
