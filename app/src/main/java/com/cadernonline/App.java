package com.cadernonline;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.cadernonline.model.Annotation;
import com.cadernonline.model.Course;
import com.cadernonline.model.Discipline;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class App extends MultiDexApplication {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        ParseObject.registerSubclass(Course.class);
        ParseObject.registerSubclass(Discipline.class);
        ParseObject.registerSubclass(Annotation.class);
        Parse.initialize(this);
        Parse.setLogLevel(BuildConfig.DEBUG ? Parse.LOG_LEVEL_VERBOSE : Parse.LOG_LEVEL_NONE);
        ParseFacebookUtils.initialize(this);

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(Exception error) {
            }
        });
    }

}