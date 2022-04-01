package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    // global variable declarations
    private EditText sEmail, sPass;
    private TextView sRegister;

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
                                Toast.makeText(SignInActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignInActivity.this, HomeActivity.class));
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


}