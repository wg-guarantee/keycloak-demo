package com.webank.app.util;

public class ValidationUtil {
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$";
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,32}$";
    
    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_PATTERN);
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && username.matches(USERNAME_PATTERN);
    }
    
    public static boolean isValidPassword(String password) {
        // 至少 8 个字符，包含大小写字母、数字和特殊字符
        if (password == null || password.length() < 8) {
            return false;
        }
        
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$");
    }
}
