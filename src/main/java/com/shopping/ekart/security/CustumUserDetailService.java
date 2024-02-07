package com.shopping.ekart.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shopping.ekart.repositary.UserRepositary;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class CustumUserDetailService implements UserDetailsService{
	
	private UserRepositary userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUserName(username).map(user->new CustomUserDetails(user))
				.orElseThrow(()->new UsernameNotFoundException("User Not Authenticated...!!!!"));
	}

	

}
