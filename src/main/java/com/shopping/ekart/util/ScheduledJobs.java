package com.shopping.ekart.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shopping.ekart.service.AuthService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobs {
	
	private AuthService authService;
	
	@Scheduled(fixedDelay = 1000l)
	void autoDelete()
	{
		authService.cleanUpNonVerifiedUsers();
		System.out.println("deleted");
	}
	

}
