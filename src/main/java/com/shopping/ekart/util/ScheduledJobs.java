package com.shopping.ekart.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shopping.ekart.service.AuthService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobs {
	
	private AuthService authService;
	
//	@Scheduled(fixedDelay = 1000l)
	
	/**
	 *  0  : Second (0-59)
	 *	0  : Minute (0-59)
	 *	0  : Hour (0-23)
	 *	?  : Day of the month (no specific value)
	 *  *  : Month (any)
	 *	MON: Day of the week (Monday)
	 */
	
	@Scheduled(cron = "0 0 0 ? * *")
	void autoDelete()
	{
		authService.cleanUpNonVerifiedUsers();
	}
	
	@Scheduled(fixedDelay = 3000L*60)
	public void callCleanupExpiredAccessTokens() {
		authService.cleanUpExpiredAccessToken();
	}
	
	@Scheduled(fixedDelay = 3000L*60)
	public void callCleanupExpiredRefreshTokens() {
		authService.cleanUpExpiredRefereshToken();
	}
	

}
