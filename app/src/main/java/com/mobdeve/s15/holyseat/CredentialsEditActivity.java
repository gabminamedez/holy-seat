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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class CredentialsEditActivity extends AppCompatActivity {

    public static final String TAG = "CredentialsEditActivity";
    public static final String PROFILE_KEY = "PROFILE_KEY";
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String PASSWORD_KEY = "PASSWORD_KEY";

    private String curEmail;

    private Button editEmailButton;
    private Button editPasswordButton;
    private ImageButton backButton;
    private TextView editEmail;
    private TextView editPassword1;
    private TextView editPassword2;
    private TextView errorEmail;
    private TextView errorPass1;
    private TextView errorPass2;
    private boolean valid;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_edit);

        this.editEmailButton = findViewById(R.id.enterCredentialsButton);
        this.editPasswordButton = findViewById(R.id.editPasswordButton);
        this.backButton = findViewById(R.id.backButton);
        this.editEmail = findViewById(R.id.editEmail);
        this.editPassword1 = findViewById(R.id.editPassword1);
        this.editPassword2 = findViewById(R.id.editPassword2);
        this.errorEmail = findViewById(R.id.errorEmail);
        this.errorPass1 = findViewById(R.id.errorPass1);
        this.errorPass2 = findViewById(R.id.errorPass2);

        Intent i = getIntent();
        String profileRefString = i.getStringExtra(PROFILE_KEY);
        String curEmail = i.getStringExtra(EMAIL_KEY);
        String curPassword = i.getStringExtra(PASSWORD_KEY);

        editEmail.setText(curEmail);

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference profileRef = db.collection("Users").document(profileRefString);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CredentialsEditActivity.this, ProfileEditActivity.class);
                intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                startActivity(intent);
                finish();
            }
        });

        editEmailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();

                invalid();
                if (email.isEmpty() || !isValidEmail(email)) {
                    errorEmail.setText(R.string.error_email);
                    errorEmail.setVisibility(View.VISIBLE);
                }
                else {
                    db.collection("Users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (document.getString("email").equals(curEmail)) {
                                            errorEmail.setVisibility(View.GONE);
                                            valid();
                                            if (isValid()) {
                                                AuthCredential credential = EmailAuthProvider.getCredential(curEmail, curPassword);
                                                user.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Log.d(TAG, "User re-authenticated.");
                                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                user.updateEmail(email)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Log.d(TAG, "User email address updated.");
                                                                                    db.document("Users/" + profileRefString).update("email", email);
                                                                                    Intent intent = new Intent(CredentialsEditActivity.this, ProfileActivity.class);
                                                                                    intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        }
                                        else {
                                            errorEmail.setText(R.string.error_email_exist);
                                            errorEmail.setVisibility(View.VISIBLE);
                                            invalid();
                                            break;
                                        }
                                    }
                                }
                                else if (task.getResult().isEmpty()) {
                                    errorEmail.setVisibility(View.GONE);
                                    valid();
                                    if (isValid()) {
                                        AuthCredential credential = EmailAuthProvider.getCredential(curEmail, curPassword);
                                        user.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d(TAG, "User re-authenticated.");
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        user.updateEmail(email)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d(TAG, "User email address updated.");
                                                                            db.document("Users/" + profileRefString).update("email", email);
                                                                            Intent intent = new Intent(CredentialsEditActivity.this, ProfileActivity.class);
                                                                            intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        editPasswordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String password = editPassword1.getText().toString().trim();
                String confirmPass = editPassword2.getText().toString().trim();

                valid();

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
                    AuthCredential credential = EmailAuthProvider.getCredential(curEmail, curPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "User re-authenticated.");
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.updatePassword(password)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User password updated.");
                                                        Intent intent = new Intent(CredentialsEditActivity.this, ProfileActivity.class);
                                                        intent.putExtra(ProfileActivity.PROFILE_KEY, profileRefString);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            });
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