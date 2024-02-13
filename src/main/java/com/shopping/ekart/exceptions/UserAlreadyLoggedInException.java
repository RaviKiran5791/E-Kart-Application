package com.shopping.ekart.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class UserAlreadyLoggedInException extends RuntimeException {
	private String message;

}
