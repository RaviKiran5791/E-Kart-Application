package com.shopping.ekart.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
	
	
	@NotEmpty(message = "email cannot be not null & not blank")
	@Email(regexp = "[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}", message = "invalid email")
	private String email;
	
	@NotEmpty(message = "UserName is Required!!")
	@Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
	message = "Password must"+ " contain at least one letter, one number, one special character")
	private String password;
	
	@NotEmpty(message = "User role cannot be empty")
	@Pattern(regexp = "^(SELLER|CUSTOMER)$",message = "Plese mention SELLER or CUSTOMER")
	private String userRole;

}
