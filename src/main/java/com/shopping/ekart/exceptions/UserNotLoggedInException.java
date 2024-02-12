package com.shopping.ekart.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class UserNotLoggedInException extends RuntimeException {
	private String message;

}
