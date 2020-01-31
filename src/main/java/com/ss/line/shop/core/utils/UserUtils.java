// package com.ss.line.shop.core.utils;

// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;

// public class UserUtils {
	
// 	public static String getUsername() {
// 		return SecurityContextHolder.getContext().getAuthentication().getName();
// 	}
	
// 	public static String getCompanyCode() {
// 		return "000";
// 	}
	
// 	public static String hashPassword(String passWord) throws Exception {
// 		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
// 		String hashPassword = passwordEncoder.encode(passWord);
// 		return hashPassword;
// 	}
// }