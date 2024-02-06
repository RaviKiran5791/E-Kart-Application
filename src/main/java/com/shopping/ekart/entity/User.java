package com.shopping.ekart.entity;

import com.shopping.ekart.enums.USERROLE;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
	@Id
	private int userId;
	private String userName;
	private String email;
	private String password;
	boolean isEmailVerified;
	boolean isDeleted;
	@Enumerated(EnumType.STRING)
	private USERROLE userRole;
	

}
