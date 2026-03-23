package com.vastpro.sphinx.util;

import org.apache.sshd.common.config.keys.loader.openssh.kdf.BCrypt;

public class PasswordHashing {
	public static String encryptPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(4));
	}
	public static boolean checkPassword(String password, String hashedPassword) {
		return BCrypt.checkpw(password, hashedPassword);
	}
}
