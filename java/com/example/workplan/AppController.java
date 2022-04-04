package com.example.workplan;

import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.messaging.FirebaseMessaging;

public class AppController extends Application {


    public static final String TAG = AppController.class
            .getSimpleName();
    private static AppController mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //  used to send notificaiton to the all employees
        FirebaseMessaging.getInstance().subscribeToTopic("AllUsers");

    }


    public static synchronized AppController getInstance() {
        return mInstance;
    }



}
