package com.example.gadgetzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gadgetzone.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(RegisterActivity.class.
            getPackage()).toString();
    private static final int REG_KEY = 159753;
    private FirebaseFirestore db;
    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;
    EditText phoneNumberEditText;
    EditText addressEditText;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordAgainEditText = findViewById(R.id.passwordAgainEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        usernameEditText.setText(username);
        passwordEditText.setText(password);
        passwordAgainEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String userName = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordAgainEditText.getText().toString();
        String phone = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "Passwords don't match! Try again.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        User user = new User(userName, email, phone, address);

                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(documentReference -> Log.d(LOG_TAG,
                                        "DocumentSnapshot added with ID: " + documentReference.getId()))
                                .addOnFailureListener(e -> Log.w(LOG_TAG, "Error adding document", e));

                        startWebShop();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Account wasn't created " +
                                        "successfully: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void cancel(View view) {
        finish();
    }

    private void startWebShop() {
        Intent intent = new Intent(this, WebshopActivity.class);
        intent.putExtra("REG_KEY", REG_KEY);
        startActivity(intent);
    }

}
