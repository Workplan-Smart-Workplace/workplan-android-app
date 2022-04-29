package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workplan.Model.EmployeeModel;
import com.example.workplan.location.LocationUpdatesService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    // global variable declarations
    private EditText sEmail, sPass;
    private TextView sRegister;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            StartService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private void StartService() {

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            mService.requestLocationUpdates();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        // here we check if user currently login , open dashboard directly
        if(Session.isLogin(SignInActivity.this)){
            Opendashboardpage();
        }
    }


    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    // firebase
    private FirebaseAuth fAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // edit text fields and register button
        sEmail = findViewById(R.id.email);
        sPass = findViewById(R.id.password);
        sRegister = findViewById(R.id.nav);

        // declare firebase auth
        fAuth = FirebaseAuth.getInstance();




        // set register text to nav to register activity
        sRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navToReg();
            }
        });


    }

    private void Opendashboardpage() {
        /// this line work without fragments
        //   startActivity(new Intent(SignInActivity.this, HomeActivity.class));

        // this line with fragments
        startActivity(new Intent(SignInActivity.this, HomeActivitywithFragments.class));
        finish();
    }

    // navigate to register activity
    public void navToReg(){
        Intent i = new Intent(SignInActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    // log in onClick (XML) from the Sign In Button
    public void loginUser(View view){
        // get email and password from editText fields
        String email = sEmail.getText().toString();
        String pass = sPass.getText().toString();

        // error cases
        // checks for empty/valid email and password fields
        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            if (!pass.isEmpty()){
                // if error cases passed, signs in with email and pw
                fAuth.signInWithEmailAndPassword(email, pass) // signs in
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                DocumentReference userDetails = FirebaseFirestore.getInstance().collection("users").document(authResult.getUser().getUid());
                                userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        try {

                                            EmployeeModel employeeModel = documentSnapshot.toObject(EmployeeModel.class);
                                            Session.saveUserDetail(SignInActivity.this,employeeModel);
                                            Toast.makeText(SignInActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                                            Opendashboardpage();

                                        } catch (Exception e) {
                                            Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignInActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                sPass.setError("Please enter a password.");
            }
        }
        else if (email.isEmpty()){
            sEmail.setError("Please enter an email.");
        }
        else {
            sEmail.setError("Please enter a valid email.");
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

        } else {
            //  Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SignInActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                // Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {


            }
        }
    }






}