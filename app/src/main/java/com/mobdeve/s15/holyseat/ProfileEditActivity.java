package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ProfileEditActivity extends AppCompatActivity {

    public static final String TAG = "ProfileEditActivity";
    public static final String PROFILE_KEY = "PROFILE_KEY";

    private String curEmail;
    private String curUsername;

    private Button editButton;
    private ImageButton backButton;
    private TextView editEmail;
    private TextView editName;
    private TextView editUsername;
    private TextView editPassword1;
    private TextView editPassword2;
    private TextView errorEmail;
    private TextView errorName;
    private TextView errorUser;
    private TextView errorPass1;
    private TextView errorPass2;
    private boolean valid;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        this.editButton = findViewById(R.id.editButton);
        this.backButton = findViewById(R.id.backButton);
        this.editEmail = findViewById(R.id.editEmail);
        this.editName = findViewById(R.id.editName);
        this.editUsername = findViewById(R.id.editUsername);
        this.editPassword1 = findViewById(R.id.editPassword1);
        this.editPassword2 = findViewById(R.id.editPassword2);
        this.errorEmail = findViewById(R.id.errorEmail);
        this.errorName = findViewById(R.id.errorName);
        this.errorUser = findViewById(R.id.errorUser);
        this.errorPass1 = findViewById(R.id.errorPass1);
        this.errorPass2 = findViewById(R.id.errorPass2);

        Intent i = getIntent();
        String profileRefString = i.getStringExtra(PROFILE_KEY);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference profileRef = db.collection("Users").document(profileRefString);
        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        curEmail = user.getEmail();
                        curUsername = user.getUsername();
                        editEmail.setText(user.getEmail());
                        editName.setText(user.getDisplayName());
                        editUsername.setText(user.getUsername());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                startActivity(intent);
                finish();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();
                String displayName = editName.getText().toString().trim();
                String userName = editUsername.getText().toString().trim();
                String password = editPassword1.getText().toString().trim();
                String confirmPass = editPassword2.getText().toString().trim();

                valid();
                if (email.isEmpty() || !isValidEmail(email)) {
                    errorEmail.setText(R.string.error_email);
                    errorEmail.setVisibility(View.VISIBLE);
                    invalid();
                }
                else {
                    db.collection("Users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (!document.getString("email").equals(curEmail)) {
                                        Toast.makeText(ProfileEditActivity.this, "ops email", Toast.LENGTH_SHORT).show();
                                        errorEmail.setText(R.string.error_email_exist);
                                        errorEmail.setVisibility(View.VISIBLE);
                                        invalid();
                                        break;
                                    }
                                    else {
                                        errorEmail.setVisibility(View.GONE);
                                    }
                                }
                            }
                            else {
                                errorEmail.setVisibility(View.GONE);
                            }
                        }
                    });
                }

                if (displayName.isEmpty() || !isValidDisplay(displayName)) {
                    errorName.setVisibility(View.VISIBLE);
                    invalid();
                }
                else {
                    errorName.setVisibility(View.GONE);
                }

                if (userName.isEmpty() || userName.length() < 6 || !isValidUser(userName)){
                    errorUser.setVisibility(View.VISIBLE);
                    invalid();
                }
                else {
                    db.collection("Users").whereEqualTo("username", userName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (!document.getString("username").equals(curUsername)) {
                                        errorUser.setText(R.string.error_user_unique);
                                        errorUser.setVisibility(View.VISIBLE);
                                        invalid();
                                        break;
                                    }
                                    else {
                                        errorUser.setVisibility(View.GONE);
                                    }
                                }
                            }
                            else {
                                errorUser.setVisibility(View.GONE);
                            }
                        }
                    });
                }

                if (password.isEmpty() || password.length() < 8) {
                    errorPass1.setVisibility(View.VISIBLE);
                    invalid();
                }
                else {
                    errorPass1.setVisibility(View.GONE);
                }

                if (confirmPass.isEmpty()|| !password.equals(confirmPass)) {
                    errorPass2.setVisibility(View.VISIBLE);
                    invalid();
                }
                else {
                    errorPass2.setVisibility(View.GONE);
                }

                if (isValid()) {
                    mAuth.getCurrentUser().delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account deleted.");
                                    }
                                }
                            });

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        db.document("Users/" + profileRefString)
                                                .update("displayName", displayName,
                                                        "email", email,
                                                        "username", userName)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error updating document", e);
                                                    }
                                                });

//                                        mAuth.getCurrentUser().updatePassword(password)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.d(TAG, "Password successfully updated!");
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Log.w(TAG, "Error updating password", e);
//                                                    }
//                                                });

                                        Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                                        intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
//                    db.document("Users/" + profileRefString)
//                            .update("displayName", displayName,
//                                    "email", email,
//                                    "username", userName)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w(TAG, "Error updating document", e);
//                                }
//                            });
//
//                    mAuth.getCurrentUser().updatePassword(password)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Log.d(TAG, "Password successfully updated!");
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w(TAG, "Error updating password", e);
//                                }
//                            });
//
//                    Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
//                    intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
//                    startActivity(intent);
//                    finish();

                }
                else {
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