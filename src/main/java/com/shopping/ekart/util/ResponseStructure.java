package com.shopping.ekart.util;

import org.springframework.stereotype.Component;

@Component
public class ResponseStructure<T> {
	
	private int statusCode;
	private String message;
	private T data;

}
