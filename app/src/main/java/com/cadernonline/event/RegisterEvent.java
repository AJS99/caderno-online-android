package com.cadernonline.event;

public class RegisterEvent {
    public final String name;
    public final String email;
    public final String password;

    public RegisterEvent(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }

}