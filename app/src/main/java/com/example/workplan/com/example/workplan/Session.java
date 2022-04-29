package com.example.workplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.workplan.Model.EmployeeModel;


public class Session {


    ///  User Session
    public static final String USERID = "smartofficeapp_userID";
    public static final String EMAIL = "smartofficeapp_email";
    public static final String FNAME = "smartofficeapp_fName";
    public static final String LOGIN = "smartofficeapp_login";
    public static final String LANME = "smartofficeapp_lName";
    public static final String MANGER = "smartofficeapp_manager";


    public static void saveUserDetail(Context context, EmployeeModel registerCrate) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(FNAME,registerCrate.getfName());
        editor.putString(LANME,registerCrate.getlName());
        editor.putBoolean(MANGER,registerCrate.getisManger());
        editor.putBoolean(LOGIN, true);
        editor.apply();
    }
    public static void clearUserSession(Context mContext) {
        if (mContext != null) {
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            if (mSharedPreferences != null) {


                mSharedPreferences.edit().remove(FNAME).apply();
                mSharedPreferences.edit().remove(LANME).apply();
                mSharedPreferences.edit().remove(MANGER).apply();
                mSharedPreferences.edit().remove(LOGIN).apply();



            }
        }
    }

    public static void setUserData(String key, String value, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public static String getUserData(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }

    public static boolean isLogin(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(LOGIN, false);
    }
    public static boolean isManger(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(MANGER, false);
    }
}
