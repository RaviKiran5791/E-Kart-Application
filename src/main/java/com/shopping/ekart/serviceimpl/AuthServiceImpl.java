package com.shopping.ekart.serviceimpl;


import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import com.shopping.ekart.cache.CacheStore;
import com.shopping.ekart.entity.AccessToken;
import com.shopping.ekart.entity.Customer;
import com.shopping.ekart.entity.RefreshToken;
import com.shopping.ekart.entity.Seller;
import com.shopping.ekart.entity.User;
import com.shopping.ekart.enums.USERROLE;
import com.shopping.ekart.exceptions.IllegalRequestException;
import com.shopping.ekart.exceptions.UserAlreadyExistByEmailException;
import com.shopping.ekart.exceptions.UserNotLoggedInException;
import com.shopping.ekart.repositary.AccessTokenRepositary;
import com.shopping.ekart.repositary.CustomerRepositary;
import com.shopping.ekart.repositary.RefreshTokenRepositary;
import com.shopping.ekart.repositary.SellerRepositary;
import com.shopping.ekart.repositary.UserRepositary;
import com.shopping.ekart.requestdto.AuthRequest;
import com.shopping.ekart.requestdto.OtpModel;
import com.shopping.ekart.requestdto.UserRequest;
import com.shopping.ekart.responsedto.AuthResponse;
import com.shopping.ekart.responsedto.UserResponse;
import com.shopping.ekart.security.JwtService;
import com.shopping.ekart.service.AuthService;
import com.shopping.ekart.util.CookieManager;
import com.shopping.ekart.util.MessageStructure;
import com.shopping.ekart.util.ResponseStructure;
import com.shopping.ekart.util.SimpleResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService{

	private UserRepositary userRepo;

	private SellerRepositary sellerRepo;

	private CustomerRepositary customerRepo;

	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authStructure;

	private PasswordEncoder passwordEncoder;

	// storing user and otp data in cache memory for otp register validation
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;

	private JavaMailSender javaMailSender; // Automatically bean created by spring boot mail

	private AuthenticationManager authenticationManager;

	private CookieManager cookieManager;

	private JwtService jwtService;

	private AccessTokenRepositary accessTokenRepo;

	private RefreshTokenRepositary refreshTokenRepo;

	@Value("${myapp.refresh.expiry}")
	private int refereshExpiryInSeconds;

	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;




	public AuthServiceImpl(UserRepositary userRepo, 
			SellerRepositary sellerRepo, CustomerRepositary customerRepo,
			ResponseStructure<UserResponse> structure, PasswordEncoder passwordEncoder,
			CacheStore<String> otpCacheStore, CacheStore<User> userCacheStore, 
			JavaMailSender javaMailSender,
			AuthenticationManager authenticationManager, CookieManager cookieManager, 
			JwtService jwtService,
			AccessTokenRepositary accessTokenRepo, RefreshTokenRepositary refreshTokenRepo,ResponseStructure<AuthResponse> authStructure) {
		super();
		this.userRepo = userRepo;
		this.sellerRepo = sellerRepo;
		this.customerRepo = customerRepo;
		this.structure = structure;
		this.passwordEncoder = passwordEncoder;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
		this.authStructure=authStructure;
	}



	//-----------------------------------------------------------------------------------------------------------------------------------------------------

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

	/*
	 * If the method is returning something, then it would be SYNCHRONOUS.  
	 * If the method has to send mail to multiple clients, then it should be ASYNCHRONOUS for the better performance
	 * without any delay. 
	 * 
	 * Here the daemon thread works to make it async.
	 * 
	 * @param message
	 */


	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {
		String userName=authRequest.getEmail().split("@")[0];

		UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(userName, authRequest.getPassword());

		Authentication authentication = authenticationManager.authenticate(token);
		if(!authentication.isAuthenticated())
			throw new UsernameNotFoundException("Failed to Authenticate the User");

		// generating the cookies and authresponse and returning to the client.
		else 
			return userRepo.findByUserName(userName).map(user->{

				grantAccess(response, user);

				return ResponseEntity.ok(authStructure.setStatusCode(HttpStatus.OK.value())
						.setData(AuthResponse.builder()
								.userId(user.getUserId())
								.userName(userName)
								.userRole(user.getUserRole().name())
								.isAuthenticated(true)
								.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
								.refereshExpiration(LocalDateTime.now().plusSeconds(refereshExpiryInSeconds))
								.build())
						.setMessage("Login Successfull...!!!!!!!"));

			}).get();
	}

	@Override
	public ResponseEntity<SimpleResponseStructure> logut(String at,String rt,HttpServletResponse httpServletResponse) {

		
//		String at = null;
//		String rt= null; 		
//		Cookie[] cookies = httpServletRequest.getCookies();
//
//		for(Cookie cookie:cookies)
//		{
//			if(cookie.getName().equals("rt")) 
//				rt=cookie.getValue();
//			if(cookie.getName().equals("at")) 
//				at=cookie.getValue();
//		}
		// for traditional approach
		
		if(at==null && rt==null)
			throw new UserNotLoggedInException("User Not Logged In, Plese Login First");
		
		accessTokenRepo.findByToken(at).ifPresent(accessToken->{
			accessToken.setBlocked(true);
			accessTokenRepo.save(accessToken);
			
		});
		
		refreshTokenRepo.findByToken(rt).ifPresent(refreshToken->{
			refreshToken.setBlocked(true);
			refreshTokenRepo.save(refreshToken);
		});
		
		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
		
		SimpleResponseStructure structure=new SimpleResponseStructure();
		structure.setMessage("Logged out Successfully..!!!");
		structure.setStatusCode(HttpStatus.OK.value());
	
		
		return new ResponseEntity<SimpleResponseStructure>(structure,HttpStatus.OK);
	}

	@Override
	public void cleanUpNonVerifiedUsers() {

		List<User> list = userRepo.findByIsEmailVerifiedFalse();

		if(!list.isEmpty())
		{
			list.forEach(user->userRepo.delete(user));
		}		

	}
	
	


	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------

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
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage, true); // is the mail contains multipart file
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(),true); // if the text contains html file: true
		javaMailSender.send(mimeMessage);

	}
	private void sendOtpToMail(User user,String otp) throws MessagingException
	{

		sentMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Complete Your Registration To E-Kart")
				.sentDate(new Date())
				.text("het,"+user.getUserName()+" Good to see you intrested in E-Kart, "
						+" Complete your registration process using the otp <br>"
						+"<h1>"+otp+"</h1><br>"
						+"<h2>Note : OTP expires in 1 minute </h2>"
						+"<br><br>"
						+"with best regards <br>"
						+ "E-Kart")
				.build());
	}
	private void sendRegistrationSuccessfullMail(User user) throws MessagingException
	{

		sentMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Registration Successfull..!!! - E-Kart")
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

	private void grantAccess(HttpServletResponse response,User user) {

		// generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());


		// adding access and referesh tokens cookies to the response
		response.addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), refereshExpiryInSeconds));

		// saving the access and refresh cookie in the database

		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.user(user)
				.build());

		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refereshExpiryInSeconds))
				.user(user)
				.build());
	}



	@Override
	public void cleanUpExpiredAccessToken() {
		System.out.println("STARTS -> cleanupExpiredAccessTokens()");
		accessTokenRepo.deleteAll(accessTokenRepo.findAllByExpirationBefore(LocalDateTime.now()));
		System.out.println("ENDS -> cleanupExpiredAccessTokens()");
	}



	@Override
	public void cleanUpExpiredRefereshToken() {
		System.out.println("STARTS -> cleanupExpiredRefreshTokens()");
		refreshTokenRepo.deleteAll(refreshTokenRepo.findAllByExpirationBefore(LocalDateTime.now()));
		System.out.println("ENDS -> cleanupExpiredRefreshTokens()");

		
	}






}
