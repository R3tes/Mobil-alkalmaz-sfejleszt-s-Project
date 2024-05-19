package com.example.gadgetzone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import com.example.gadgetzone.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final int REG_KEY = 159753;
    private FirebaseFirestore db;
    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;
    EditText phoneNumberEditText;
    EditText addressEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView titleTextView = findViewById(R.id.registrationTextView);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        titleTextView.startAnimation(fadeIn);

        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordAgainEditText = findViewById(R.id.passwordAgainEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordAgainEditText.getText().toString();
        String phone = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(RegisterActivity.this, "Passwords don't match! Try again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        User user = new User(username, email, phone, address);

                        // Convert the drawable to a bitmap
                        Drawable drawable = ContextCompat.getDrawable(this,
                                R.drawable.default_profile_picture);
                        assert drawable != null;
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        // Create a reference to the file in Firebase Storage
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference userImageRef = storageRef.child("images/" +
                                Objects.requireNonNull(mAuth.getCurrentUser()).getUid() + ".png");

                        // Upload the file to Firebase Storage
                        UploadTask uploadTask = userImageRef.putBytes(data);
                        uploadTask.addOnFailureListener(exception -> {

                            Log.w(LOG_TAG, "Error uploading image to Firebase Storage", exception);
                        }).addOnSuccessListener(taskSnapshot -> {
                            Log.d(LOG_TAG, "Image uploaded to Firebase Storage");

                            // Get the download URL and set it as the profile picture
                            userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                user.setProfilePicture(uri.toString());

                                db.collection("users")
                                        .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> Log.d(LOG_TAG,
                                                "User details added to Firestore"))
                                        .addOnFailureListener(e -> Log.w(LOG_TAG,
                                                "Error adding user details to Firestore", e));
                            });
                        });

                        startLoginActivity();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Account wasn't created " +
                                        "successfully: " +
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onBackPressed(View view) {
        finish();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
