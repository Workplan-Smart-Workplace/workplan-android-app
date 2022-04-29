package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // global variable declarations
    private EditText rEmail, rPass, rFName, rLName;
    private TextView rSignIn;
    private Switch rManager;
    private String userID;

    // firebase
    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // edit text fields and sign in button
        rEmail = findViewById(R.id.regEmail);
        rPass = findViewById(R.id.regPassword);
        rFName = findViewById(R.id.firstName);
        rLName = findViewById(R.id.lastName);
        rManager = findViewById(R.id.managerSwitch);
        rSignIn = findViewById(R.id.nav);

        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // set sign in text to nav to sign in activity
        rSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navToSignIn();
            }
        });

    }

    // navigate to sign in activity
    public void navToSignIn(){
        finish();
    }

    // register onClick (XML) from register button
    public void registerUser(View view){
        // get text field values and put them in local variables
        String email = rEmail.getText().toString();
        String pass = rPass.getText().toString();
        String fName = rFName.getText().toString();
        String lName = rLName.getText().toString();
        boolean manager = rManager.isChecked();

        // checks first name or last name being empty
        if (fName.isEmpty() || lName.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Incomplete Fields", Toast.LENGTH_SHORT).show();
        }
        else {
            // error cases
            // checks for empty/valid email and password fields
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                if (!pass.isEmpty()){
                    // if error cases passed, creates new user with email and pw with auth
                    fAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(RegisterActivity.this, "Registered", Toast.LENGTH_SHORT).show();

                                    // saves extra user data into hashmap
                                    userID = fAuth.getCurrentUser().getUid();
                                    // store manger as boolen not a string
                                    // also in database we have both value string and bolean
                                    /// so you have to update all node in firestore database  to boolean

                                    //String isManager = String.valueOf(manager);
                                    boolean isLoggedIn = false;
                                    Map<String,Object> userMap = new HashMap<>();

                                    // hashmap user data
                                    userMap.put("fName", fName);
                                    userMap.put("lName", lName);
                                    userMap.put("manager", manager);
                                    userMap.put("userID", userID);
                                    userMap.put("isLoggedIn", isLoggedIn);

                                    // saves user data into document under current userID
                                    DocumentReference documentReference = fStore.collection("users").document(userID);
                                    documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.i("info", "User created in Firestore");
                                        }
                                    });
                                    // navigates to sign in activity upon completion
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Registration Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    rPass.setError("Please enter a password.");
                }
            }
            else if (email.isEmpty()){
                rEmail.setError("Please enter an email.");
            }
            else {
                rEmail.setError("Please enter a valid email.");
            }
        }


    }
}