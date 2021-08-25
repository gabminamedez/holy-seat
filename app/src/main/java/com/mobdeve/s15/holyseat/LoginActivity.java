package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextView toRegister;
    private TextInputEditText loginEmail;
    private TextInputEditText loginPassword;
    private TextView errorLogin;
    private Button loginButton;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toRegister = findViewById(R.id.hasAccount2);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        errorLogin = findViewById(R.id.errorLogin);
        loginButton = findViewById(R.id.loginButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        this.sp = PreferenceManager.getDefaultSharedPreferences(this);
        this.editor = sp.edit();


        toRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();
                Log.d(TAG, "onClick: " + email);
                Log.d(TAG, "onClick: " + password);

                if (email.isEmpty() || password.isEmpty()){
                    errorLogin.setVisibility(View.VISIBLE);
                }
                else{
                    if (!isValidEmail(email)){
                        db.collection("Users").whereEqualTo("username", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        email = document.get("email").toString();
                                        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                editor.putString(ProfileActivity.PROFILE_KEY, document.getId());
                                                editor.commit();
                                                startActivity(i);
                                                finish();
                                                Log.d(TAG, "onSuccess: User authenticated");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                errorLogin.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                    else{
                        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                db.collection("Users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        editor.putString(ProfileActivity.PROFILE_KEY, document.getId());
                                                    }
                                                    editor.commit();
                                                    startActivity(i);
                                                    finish();
                                                    Log.d(TAG, "onComplete: User found");
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                                Log.d(TAG, "onSuccess: User authenticated");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errorLogin.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        });
    }
    // Adapted from: https://www.geeksforgeeks.org/check-email-address-valid-not-java/
    public static boolean isValidEmail(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}