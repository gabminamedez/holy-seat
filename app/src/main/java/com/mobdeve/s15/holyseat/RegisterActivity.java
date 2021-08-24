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
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextView toLogin;
    private TextInputEditText registerEmail;
    private TextInputEditText registerDisplayName;
    private TextInputEditText registerUsername;
    private TextInputEditText registerPass;
    private TextInputEditText registerConfirmPass;
    private TextView errorEmail;
    private TextView errorName;
    private TextView errorUser;
    private TextView errorPass1;
    private TextView errorPass2;
    private Button registerButton;
    private boolean valid;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toLogin = findViewById(R.id.hasAccount2);
        registerEmail = findViewById(R.id.registerEmail);
        registerDisplayName = findViewById(R.id.registerDisplayName);
        registerUsername = findViewById(R.id.registerUsername);
        registerPass = findViewById(R.id.registerPass);
        registerConfirmPass = findViewById(R.id.registerConfirmPass);
        registerButton = findViewById(R.id.registerButton);
        errorEmail = findViewById(R.id.errorEmail);
        errorName = findViewById(R.id.errorName);
        errorUser = findViewById(R.id.errorUser);
        errorPass1 = findViewById(R.id.errorPass1);
        errorPass2 = findViewById(R.id.errorPass2);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        this.sp = PreferenceManager.getDefaultSharedPreferences(this);
        this.editor = sp.edit();

        toLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString().trim();
                String displayName = registerDisplayName.getText().toString().trim();
                String userName = registerUsername.getText().toString().trim();
                String password = registerPass.getText().toString().trim();
                String confirmPass = registerConfirmPass.getText().toString().trim();
//                Log.d(TAG, "onClick: " + email);
//                Log.d(TAG, "onClick: " + displayName);
//                Log.d(TAG, "onClick: " + userName);
//                Log.d(TAG, "onClick: " + password);
//                Log.d(TAG, "onClick: " + confirmPass);
                valid();
                if (email.isEmpty() || !isValidEmail(email)){
                    errorEmail.setText(R.string.error_email);
                    errorEmail.setVisibility(View.VISIBLE);
                    invalid();
                }
                else{
                    mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task)
                        {
                            Log.d(TAG, "onComplete: " + task.getResult().getSignInMethods());

                            if (task.isSuccessful() && !task.getResult().getSignInMethods().isEmpty()){
                                errorEmail.setText(R.string.error_email_exist);
                                errorEmail.setVisibility(View.VISIBLE);
                                invalid();
                            } else {
                                errorEmail.setVisibility(View.GONE);
                            }

                        }
                    });
                }

                if (displayName.isEmpty() || !isValidDisplay(displayName)){
                    errorName.setVisibility(View.VISIBLE);
                    invalid();
                }
                else
                    errorName.setVisibility(View.GONE);
                if (userName.isEmpty() || userName.length() < 6 || !isValidUser(userName)){
                    errorUser.setVisibility(View.VISIBLE);
                    invalid();
                }
                else{
                    db.collection("Users").whereEqualTo("username", userName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()){
                                errorUser.setText(R.string.error_user_unique);
                                errorUser.setVisibility(View.VISIBLE);
                                invalid();
                            }
                            else
                                errorUser.setVisibility(View.GONE);
                        }
                    });
                }
                if (password.isEmpty() || password.length() < 8){
                    errorPass1.setVisibility(View.VISIBLE);
                    invalid();
                }
                else
                    errorPass1.setVisibility(View.GONE);
                if (confirmPass.isEmpty()|| !password.equals(confirmPass)){
                    errorPass2.setVisibility(View.VISIBLE);
                    invalid();
                }
                else
                    errorPass2.setVisibility(View.GONE);
                if (isValid()){
                    Log.d(TAG, "onClick: Creating User Account");
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Map<String, Object> newUser = new HashMap<>();
                                newUser.put("username", userName);
                                newUser.put("email", email);
                                newUser.put("displayName", displayName);
                                newUser.put("numReviews", 0);
                                newUser.put("created", FieldValue.serverTimestamp());
                                db.collection("Users")
                                        .add(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                                editor.putString(ProfileActivity.PROFILE_KEY, documentReference.getId());
                                                editor.commit();
                                                startActivity(i);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });
                            }
                            else{
                                Log.d(TAG, "onComplete: User not registered.");
                            }
                        }
                    });

                }
                else{
                    Log.d(TAG, "onClick: User register failed.");
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

    public static boolean isValidDisplay(String displayName){
        String nameRegex = "^[a-zA-Z ]*$";

        Pattern pat = Pattern.compile(nameRegex);
        if (displayName == null)
            return false;
        return pat.matcher(displayName).matches();
    }

    public static boolean isValidUser(String username){
        String nameRegex = "^[a-zA-Z0-9]+$";

        Pattern pat = Pattern.compile(nameRegex);
        if (username == null)
            return false;
        return pat.matcher(username).matches();
    }

    public void invalid(){
        valid = false;
    }
    public void valid(){
        valid = true;
    }
    public boolean isValid(){
        return valid;
    }

}