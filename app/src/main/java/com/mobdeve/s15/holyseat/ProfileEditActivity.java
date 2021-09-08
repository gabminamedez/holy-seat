package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String curName;
    private String curUsername;

    private Button editDetailsButton;
    private Button enterCredentialsButton;
    private ImageButton backButton;
    private TextView editName;
    private TextView editUsername;
    private TextView errorName;
    private TextView errorUser;
    private boolean valid;

    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        this.editDetailsButton = findViewById(R.id.editDetailsButton);
        this.enterCredentialsButton = findViewById(R.id.enterCredentialsButton);
        this.backButton = findViewById(R.id.backButton);
        this.editName = findViewById(R.id.editName);
        this.editUsername = findViewById(R.id.editUsername);
        this.errorName = findViewById(R.id.errorName);
        this.errorUser = findViewById(R.id.errorUser);

        Intent i = getIntent();
        String profileRefString = i.getStringExtra(PROFILE_KEY);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        DocumentReference profileRef = db.collection("Users").document(profileRefString);
        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        curEmail = user.getEmail();
                        curName = user.getDisplayName();
                        curUsername = user.getUsername();
                        editName.setText(curName);
                        editUsername.setText(curUsername);
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

        editDetailsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String displayName = editName.getText().toString().trim();
                String userName = editUsername.getText().toString().trim();

                valid();

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

                if (isValid()) {
                    db.document("Users/" + profileRefString)
                            .update("displayName", displayName,
                                    "username", userName)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    db.collection("Check Ins").whereEqualTo("userID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    db.document("Check Ins/" + document.getId()).update("userName", displayName);
                                                }
                                            }
                                        }
                                    });
                                    db.collection("Reviews").whereEqualTo("reviewerID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    db.document("Reviews/" + document.getId()).update("reviewerName", displayName);
                                                }
                                            }
                                        }
                                    });
                                    Intent intent = new Intent(ProfileEditActivity.this, ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
                else {
                    Log.d(TAG, "onClick: User register failed.");
                }
            }
        });

        enterCredentialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProfileEditActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.credentials_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                Button enterCredentialsButton2 = dialog.findViewById(R.id.enterCredentialsButton2);
                TextView credEmail = dialog.findViewById(R.id.credEmail);
                TextView credPassword = dialog.findViewById(R.id.credPassword);
                TextView errorCred = dialog.findViewById(R.id.errorCred);
                dialog.show();

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                enterCredentialsButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = credEmail.getText().toString().trim();
                        String password = credPassword.getText().toString().trim();

                        if (email.isEmpty() || password.isEmpty() || !email.equals(curEmail) || !isValidEmail(email)) {
                            errorCred.setVisibility(View.VISIBLE);
                        }
                        else {
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent intent = new Intent(ProfileEditActivity.this, CredentialsEditActivity.class);
                                                intent.putExtra(CredentialsEditActivity.PROFILE_KEY, profileRefString);
                                                intent.putExtra(CredentialsEditActivity.EMAIL_KEY, email);
                                                intent.putExtra(CredentialsEditActivity.PASSWORD_KEY, password);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    errorCred.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

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