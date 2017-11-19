package com.cadernonline.util;

import com.parse.ParseUser;

public class AuthUtil {

    private AuthUtil() { }

    public static boolean isLoggedIn(){
        return ParseUser.getCurrentUser() != null;
    }

}