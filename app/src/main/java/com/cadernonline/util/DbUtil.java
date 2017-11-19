package com.cadernonline.util;

import com.cadernonline.App;
import com.cadernonline.R;
import com.cadernonline.model.Annotation;
import com.cadernonline.model.Course;
import com.cadernonline.model.Discipline;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DbUtil {

    private DbUtil() { }

    public static void getCourses(FindCallback<Course> callback){
        if(!AuthUtil.isLoggedIn()){
            callback.done(null, new ParseException(-1, App.context.getString(R.string.login_required)));
        } else {
            ParseQuery.getQuery(Course.class)
                    .whereEqualTo(Course.KEY_USER, ParseUser.getCurrentUser())
                    .orderByAscending(Course.KEY_NAME)
                    .findInBackground(callback);
        }
    }

    public static void getDisciplines(Course course, FindCallback<Discipline> callback){
        if(!AuthUtil.isLoggedIn()){
            callback.done(null, new ParseException(-1, App.context.getString(R.string.login_required)));
        } else {
            ParseQuery.getQuery(Discipline.class)
                    .whereEqualTo(Discipline.KEY_COURSE, course)
                    .whereEqualTo(Discipline.KEY_USER, ParseUser.getCurrentUser())
                    .orderByAscending(Discipline.KEY_NAME)
                    .findInBackground(callback);
        }
    }

    public static void getAnnotations(Discipline discipline, FindCallback<Annotation> callback){
        if(!AuthUtil.isLoggedIn()){
            callback.done(null, new ParseException(-1, App.context.getString(R.string.login_required)));
        } else {
            ParseQuery.getQuery(Annotation.class)
                    .whereEqualTo(Annotation.KEY_DISCIPLINE, discipline)
                    .whereEqualTo(Annotation.KEY_USER, ParseUser.getCurrentUser())
                    .addDescendingOrder("createdAt")
                    .addAscendingOrder(Annotation.KEY_SUBJECT)
                    .findInBackground(callback);
        }
    }

    public static void delete(ParseObject object){
        if(AuthUtil.isLoggedIn()){
            object.deleteInBackground();
        }
    }

}