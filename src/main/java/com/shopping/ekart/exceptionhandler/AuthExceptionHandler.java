package com.shopping.ekart.exceptionhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.shopping.ekart.exceptions.IllegalRequestException;
import com.shopping.ekart.exceptions.UserAlreadyExistByEmailException;
import com.shopping.ekart.exceptions.UserAlreadyLoggedOutException;
import com.shopping.ekart.exceptions.UserNotLoggedInException;

@RestControllerAdvice
public class AuthExceptionHandler extends ResponseEntityExceptionHandler{
	
	private ResponseEntity<Object> structure(HttpStatus status, String message, Object rootCause) {
		return new ResponseEntity<Object>(Map.of(

				"status", status.value(), "message", message, "rootCause", rootCause), status);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		// Extracting all validation errors from the exception
		List<ObjectError> allErrors = ex.getAllErrors();

		// Creating a Map to store field errors
		Map<String, String> errors = new HashMap();
		
		// Iterating through all validation errors
		allErrors.forEach(error -> {
			// Casting each error to FieldError
			FieldError fieldError = (FieldError) error;
			
			// Adding the field and its error message to the Map
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		});

		return structure(HttpStatus.BAD_REQUEST, "Failed To Save the data", errors);
	}

	@ExceptionHandler(UserAlreadyExistByEmailException.class)
	public ResponseEntity<Object> userAlreadyExist(UserAlreadyExistByEmailException e) {
		return structure(HttpStatus.BAD_REQUEST, e.getMessage(), "Data Already Present");
	}

	@ExceptionHandler(IllegalRequestException.class)
	public ResponseEntity<Object> illegalRequest(IllegalRequestException e) {
		return structure(HttpStatus.BAD_REQUEST, e.getMessage(), "Illegal Request..!!!!");
	}
	
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<Object> usernameNotFound(UsernameNotFoundException e) {
		return structure(HttpStatus.BAD_REQUEST, e.getMessage(), "User Name and Password Not Present..!!!!");
	}
	
	@ExceptionHandler(UserNotLoggedInException.class)
	public ResponseEntity<Object> usernameNotLoggedIn(UserNotLoggedInException e) {
		return structure(HttpStatus.BAD_REQUEST, e.getMessage(), "User Not Logged In ..!!!!");
	}
	@ExceptionHandler(UserAlreadyLoggedOutException.class)
	public ResponseEntity<Object> usernameAlreadyLoggedOut(UserAlreadyLoggedOutException e) {
		return structure(HttpStatus.BAD_REQUEST, e.getMessage(), "User Not Logged In ..!!!!");
	}


}
