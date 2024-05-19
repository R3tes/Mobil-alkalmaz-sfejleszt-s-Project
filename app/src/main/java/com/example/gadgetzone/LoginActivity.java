package com.example.gadgetzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).
            toString();

    EditText emailEditText;
    EditText passwordEditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView titleTextView = findViewById(R.id.loginTextView);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        titleTextView.startAnimation(fadeIn);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        emailEditText.setText("testuser@example.com");
        passwordEditText.setText("TestPassword123");

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        String username = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Username and password cannot be empty.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        startWebShop();
                    } else {
                        String errorMessage = getString(task);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @NonNull
    private static String getString(Task<AuthResult> task) {
        String errorMessage;
        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "Invalid username or password.";
        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid credentials. Please try again.";
        } else {
            errorMessage = "An error occurred. Please try again later.";
        }
        return errorMessage;
    }

    private void startWebShop() {
        Intent intent = new Intent(this, WebshopActivity.class);
        startActivity(intent);
        invalidateOptionsMenu();
    }

    public void loginAsGuest(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startWebShop();
            } else {
                String errorMessage = "An error occurred. Please try again later.";
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onBackPressed(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", emailEditText.getText().toString());
        editor.putString("password", passwordEditText.getText().toString());
        editor.apply();
    }
}
