package com.cadernonline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Anotacao")
public class Annotation extends ParseObject {
    public static final String KEY_SUBJECT = "assunto";
    public static final String KEY_TEXT = "texto";
    public static final String KEY_DISCIPLINE = "disciplina";
    public static final String KEY_USER = "user";

    public String getSubject() {
        return getString(KEY_SUBJECT);
    }

    public void setSubject(String value) {
        put(KEY_SUBJECT, value);
    }

    public String getText() {
        return getString(KEY_TEXT);
    }

    public void setText(String value) {
        put(KEY_TEXT, value);
    }

    public ParseObject getDiscipline() {
        return getParseObject(KEY_DISCIPLINE);
    }

    public void setDiscipline(Discipline value) {
        put(KEY_DISCIPLINE, value);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser value) {
        put(KEY_USER, value);
    }
}