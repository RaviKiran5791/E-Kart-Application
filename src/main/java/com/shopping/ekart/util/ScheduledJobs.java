package com.shopping.ekart.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shopping.ekart.service.AuthService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobs {
	
	private AuthService authService;
	
	
	/**
	 *  0  : Second (0-59)
	 *	0  : Minute (0-59)
	 *	0  : Hour (0-23)
	 *	?  : Day of the month (no specific value)
	 *  *  : Month (any)
	 *	MON: Day of the week (Monday)
	 */
	
//	@Scheduled(fixedDelay = 1000l*60*5) 
	@Scheduled(cron = "0 0 0 ? * *")
	void autoDelete()
	{
		authService.cleanUpNonVerifiedUsers();
//		System.out.println("deleted");
	}
	

}
