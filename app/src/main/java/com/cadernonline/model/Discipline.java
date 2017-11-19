package com.cadernonline.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Disciplina")
public class Discipline extends ParseObject {
    public static final String KEY_NAME = "nome";
    public static final String KEY_TEACHER = "nomeProfessor";
    public static final String KEY_DESCRIPTION = "descricao";
    public static final String KEY_COURSE = "curso";
    public static final String KEY_USER = "user";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String value) {
        put(KEY_NAME, value);
    }

    public String getTeacher() {
        return getString(KEY_TEACHER);
    }

    public void setTeacher(String value) {
        put(KEY_TEACHER, value);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String value) {
        put(KEY_DESCRIPTION, value);
    }

    public ParseObject getCourse() {
        return getParseObject(KEY_COURSE);
    }

    public void setCourse(Course value) {
        put(KEY_COURSE, value);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser value) {
        put(KEY_USER, value);
    }
}