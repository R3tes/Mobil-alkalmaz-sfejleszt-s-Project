package com.example.gadgetzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private static final int LOG_KEY = 420696;

    EditText usernameEditText;
    EditText passwordEditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                startWebShop();
            } else {
                Toast.makeText(MainActivity.this, "User log in fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startWebShop() {
        Intent intent = new Intent(this, WebShopActivity.class);
        startActivity(intent);
    }

    public void loginAsGuest(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "Guest user loged in successfully");
                startWebShop();
            } else {
                Log.d(LOG_TAG, "Anonym user log in fail");
                Toast.makeText(MainActivity.this, "User log in fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void registration(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("LOG_KEY", LOG_KEY);
        startActivity(intent);
    }
}