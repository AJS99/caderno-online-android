package com.cadernonline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Curso")
public class Course extends ParseObject {
    public static final String KEY_NAME = "nome";
    public static final String KEY_COORDINATOR = "nomeCoordenador";
    public static final String KEY_DESCRIPTION = "descricao";
    public static final String KEY_INSTITUTION = "instituicao";
    public static final String KEY_USER = "user";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String value) {
        put(KEY_NAME, value);
    }

    public String getCoordinator() {
        return getString(KEY_COORDINATOR);
    }

    public void setCoordinator(String value) {
        put(KEY_COORDINATOR, value);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String value) {
        put(KEY_DESCRIPTION, value);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser value) {
        put(KEY_USER, value);
    }
}