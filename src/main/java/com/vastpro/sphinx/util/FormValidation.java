package com.vastpro.sphinx.util;



public class FormValidation {
	  	 static final String USERNAME_REGEX = "^[a-zA-Z]\\S{4,}$";
	  	 static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	  	 static final String PASSWORD_PATTERN = "^(?=\\S{8,}$).*";

	     static final String FIRSTNAME_REGEX = "^[A-Za-z]+$";
	     static final String LASTNAME_REGEX = "^[A-Za-z]+$";
	
	public static boolean validateUsername(String username) {
		if(username!=null) {
			return username.matches(USERNAME_REGEX);
		}
		return false;	
	}
	
	public static boolean validateEmail(String email) {
		if(email!=null) {
			return email.matches(EMAIL_REGEX);
		}
		return false;		
	}
	
	
	public static boolean validatePassword(String password) {
		if(password!=null) {
			return password.matches(PASSWORD_PATTERN);
		}
		return false;		
	}
	
	public static boolean validateFirstName(String firstName) {
		if(firstName!=null) {
			return firstName.matches(FIRSTNAME_REGEX);
		}
		return false;		
	}
	
	public static boolean validateLastName(String lastName) {
		if(lastName!=null) {
			return lastName.matches(LASTNAME_REGEX);
		}
		return false;		
	}
}
