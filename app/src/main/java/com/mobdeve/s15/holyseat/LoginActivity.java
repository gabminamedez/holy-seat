package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextView toRegister;
    private TextView loginEmail;
    private TextView loginPassword;
    private TextView errorLogin;
    private Button loginButton;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();
                Log.d(TAG, "onClick: " + email);
                Log.d(TAG, "onClick: " + password);




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
                                                i.putExtra(ProfileActivity.PROFILE_KEY, document.getId());
                                            }
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
        });
    }
}