package com.vastpro.sphinx.util;

import java.security.SecureRandom;

import org.apache.sshd.common.config.keys.loader.openssh.kdf.BCrypt;

public class PasswordUtil {
	public static String encryptPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(4));
	}
	public static boolean checkPassword(String password, String hashedPassword) {
		return BCrypt.checkpw(password, hashedPassword);
	}
	
	
	public static String userGeneratePassword() {
		SecureRandom random = new SecureRandom();
		String passwordChar = "0123456789";
		StringBuilder passwordBuilder = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			passwordBuilder.append(passwordChar.charAt(random.nextInt(passwordChar.length())));
		}
		return String.valueOf(passwordBuilder);
	}
	public static String examGeneratePassword() {
		SecureRandom random = new SecureRandom();
		String passwordChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder passwordBuilder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			passwordBuilder.append(passwordChar.charAt(random.nextInt(passwordChar.length())));
		}
		return String.valueOf(passwordBuilder);
	}
}
