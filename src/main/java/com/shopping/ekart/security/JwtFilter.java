package com.shopping.ekart.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shopping.ekart.entity.AccessToken;
import com.shopping.ekart.exceptions.IllegalRequestException;
import com.shopping.ekart.exceptions.UserNotLoggedInException;
import com.shopping.ekart.repositary.AccessTokenRepositary;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter{

	private AccessTokenRepositary accessTokenRepo;

	private JwtService jwtService;

	private CustumUserDetailService custumUserDetailService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String at=null;
		String rt=null;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) 
		{ 
			for(Cookie cookie:cookies) {
				if(cookie.getName().equals("at"))
					at=cookie.getValue();
				if(cookie.getName().equals("rt"))
					rt=cookie.getValue();
			}

			String userName=null;

			if(at!=null && rt!=null) 
			{
				Optional<AccessToken> accessToken = accessTokenRepo.findByTokenAndIsBlocked(at,false);

				if(accessToken==null) throw new IllegalRequestException("User Not Logged In");
				else
				{
					log.info("Authenticating the token.....");
					userName = jwtService.extractUserName(at);
					if(userName==null)throw new IllegalRequestException("Failed To Authenticate");

					UserDetails userDetails = custumUserDetailService.loadUserByUsername(userName);

					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, null, userDetails.getAuthorities());
					token.setDetails(new WebAuthenticationDetails(request));
					SecurityContextHolder.getContext().setAuthentication(token);
					log.info("Authenticated SuccessFully");
				}
			}
		}
		filterChain.doFilter(request, response);
		
	}

}
