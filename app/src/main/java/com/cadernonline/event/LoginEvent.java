package com.cadernonline.event;

public class LoginEvent {
    public final String email;
    public final String password;

    public LoginEvent(String email, String password){
        this.email = email;
        this.password = password;
    }

}