package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workplan.Model.EmployeeModel;
import com.example.workplan.fragment.CallbacksCommunicatoer;
import com.example.workplan.fragment.DeclinedMeetingFragment;
import com.example.workplan.fragment.DeletedtaskFragment;
import com.example.workplan.fragment.HomeFragment;
import com.example.workplan.fragment.ManagerSchedulerFragment;
import com.example.workplan.fragment.NotificationFragment;
import com.example.workplan.fragment.SchedulerFragment;
import com.example.workplan.location.LocationUpdatesService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class HomeActivitywithFragments extends AppCompatActivity implements CallbacksCommunicatoer {


    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_notification = "notification";
    private static final String TAG_TASK = "task";
    private static final String TAG_DELETED_TASK = "deleted_task";
    private static final String TAG_DECLINED_MEETNG = "declined_meeting";




    public static String CURRENT_TAG = TAG_HOME;
    public static String Previous_TAG="";

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;


    private View hSignOutBtn;
    private TextView hWelcome ,hDate;














    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_with_fragments);
        hWelcome = findViewById(R.id.screenTitle);
        hDate = findViewById(R.id.regFlavorText);
        hSignOutBtn = findViewById(R.id.signOut);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
       // hDate.setText(AppController.getdate());
        mHandler = new Handler();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
        hSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Sets Date text to current Date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDate = new SimpleDateFormat("EEE, MMM d");
        String formattedDate = formatDate.format(calendar.getTime());
        hDate.setText(formattedDate);




    }


    private void signOut(){
        try {
            if(Session.isManger(HomeActivitywithFragments.this))
            FirebaseMessaging.getInstance().unsubscribeFromTopic("manager");
            FirebaseAuth.getInstance().signOut();
            // clear user session on logout button
            Session.clearUserSession(HomeActivitywithFragments.this);

        }
        catch (Exception e){

        }
        Toast.makeText(HomeActivitywithFragments.this, "Signed Out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(HomeActivitywithFragments.this, SignInActivity.class);
        startActivity(i);
        finish();
    }







    //bottom navigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    navItemIndex = 0;
                    Previous_TAG=CURRENT_TAG;
                    CURRENT_TAG = TAG_HOME;
                    loadHomeFragment();
                    return true;
                case R.id.navigation_notification:
                    navItemIndex = 1;
                    Previous_TAG=CURRENT_TAG;
                    CURRENT_TAG = TAG_notification;
                    loadHomeFragment();
                    return true;
                case R.id.navigation_task:
                    navItemIndex = 2;
                    Previous_TAG=CURRENT_TAG;
                    CURRENT_TAG = TAG_TASK;

                    loadHomeFragment();
                    return true;


            }
            return false;
        }
    };

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {


        // set toolbar title
        setToolbarTitle();

        if(Previous_TAG==CURRENT_TAG){
            //  drawer.closeDrawers();
            return;
        }

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
//        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
//            drawer.closeDrawers();
//
//
//            return;
//        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }

                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_container, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }






    }

    private void setToolbarTitle() {
        if(navItemIndex==0)
            hWelcome.setText("Welcome, "+Session.getUserData(Session.FNAME,HomeActivitywithFragments.this));

        else if (navItemIndex==1)
        hWelcome.setText("Notification");
        else if (navItemIndex==2)
        hWelcome.setText("Scheduler");
        else if (navItemIndex==3)
            hWelcome.setText("Deleted Tasks");
        else if (navItemIndex==4)
            hWelcome.setText("Declined Meetings");


    }

    private Fragment getHomeFragment() {

        switch (navItemIndex) {
            case 0:
                 // load  home fragment
                 HomeFragment homeFragment = HomeFragment.newInstance();
                return homeFragment;
            case 1:
//                // load notification fragment
                 NotificationFragment notificationfragment = NotificationFragment.newInstance("","");
                return notificationfragment;
            case 2:
//                // load task fragments fragment
                Fragment fragment;
                if(Session.isManger(HomeActivitywithFragments.this))
                    fragment = ManagerSchedulerFragment.newInstance("","");
                else
                    fragment = SchedulerFragment.newInstance("","");
                return fragment;

                case 3:
                return DeletedtaskFragment.newInstance("","");
            case 4:
                return DeclinedMeetingFragment.newInstance("","");



            default:
                return new HomeFragment();
        }
    }


    @Override
    public void onBackPressed() {

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                Previous_TAG=CURRENT_TAG;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }
        finishAffinity();

    }


    @Override
    public void DataSend(String Command) {

        if (Command.equals("DELETEDTASK")){
            navItemIndex = 3;
            Previous_TAG=CURRENT_TAG;
            CURRENT_TAG = TAG_DELETED_TASK;
            loadHomeFragment();

        }
       else if (Command.equals("DECLINEMEETING")){
            navItemIndex = 4;
            Previous_TAG=CURRENT_TAG;
            CURRENT_TAG = TAG_DECLINED_MEETNG;
            loadHomeFragment();
        }

    }
}