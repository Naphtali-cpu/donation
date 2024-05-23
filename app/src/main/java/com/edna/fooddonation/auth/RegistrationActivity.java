package com.edna.fooddonation.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.edna.fooddonation.R;
import com.edna.fooddonation.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword;
    private TextView rediSignIn;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_registration);
        initializeViews();
        setClickListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        rediSignIn = findViewById(R.id.redirectsignin);
    }

    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        rediSignIn.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (isInvalidInput(email, username, password)) {
            return;
        }

        createUserWithEmailAndPassword(email, password, username);
    }

    private boolean isInvalidInput(String email, String username, String password) {
        if (password.length() < 8) {
            showToast("Password must contain more than 8 characters!");
            return true;
        } else if (Pattern.matches("[a-zA-Z]+", password)) {
            showToast("Password must contain special characters!");
            return true;
        } else if (!email.contains("@")) {
            showToast("Email must contain @ symbol!");
            return true;
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void createUserWithEmailAndPassword(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            saveUserDataToDatabase(userId, email, username);
                        }
                    } else {
                        Log.d("REGISERR", task.getException().toString());
                        showToast(task.getException().toString());
                    }
                })
        ;
    }

    private void saveUserDataToDatabase(String userId, String email, String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        User user = new User(userId, email, username);

        usersRef.setValue(user)
                .addOnSuccessListener(aVoid -> {
                    showToast("User data saved successfully");
                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save user data"));
    }


}