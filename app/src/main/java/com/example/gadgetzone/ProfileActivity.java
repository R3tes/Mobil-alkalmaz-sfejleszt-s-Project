package com.example.gadgetzone;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.Manifest;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.example.gadgetzone.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity.class.getName();
    private static final Logger LOGGER = Logger.getLogger(ProfileActivity.class.getName());
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private SharedPreferences preferences;
    private TextView usernameTextView;
    private TextView phoneNumberTextView;
    private TextView addressTextView;
    private TextView emailTextView;
    private ActivityResultLauncher<String> mGetContent;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        fetchUserData();
        setupEditTextTransformations();
        setupImagePicker();
    }

    private void initializeViews() {
        preferences = getSharedPreferences(LoginActivity.PREF_KEY, MODE_PRIVATE);

        usernameTextView = findViewById(R.id.usernameTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        addressTextView = findViewById(R.id.addressTextView);
        emailTextView = findViewById(R.id.emailTextView);
    }

    private void fetchUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = preferences.getString("userId", null);
        if (userId != null) {
            DocumentReference docRef = db.collection("users").document(userId);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        updateUIWithUserData(document);
                        fetchUserProfilePicture(userId);
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            });
        }
    }

    private void updateUIWithUserData(DocumentSnapshot document) {
        usernameTextView.setText(getString(R.string.username_label,
                document.getString("username")));
        phoneNumberTextView.setText(getString(R.string.phone_number_label,
                document.getString("phone")));
        addressTextView.setText(getString(R.string.address_label,
                document.getString("address")));
        emailTextView.setText(document.getString("email"));
    }

    private void fetchUserProfilePicture(String userId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference userImageRef = storage.getReference().child(
                "images/" + userId + ".png");

        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // The uri variable contains the download URL of the image
            String profilePictureUrl = uri.toString();

            Log.d(LOG_TAG, "Profile picture URL: " + profilePictureUrl);

            // Load the profile image into the ImageView
            ImageView profileImageView = findViewById(R.id.profileImageView);
            Glide.with(this)
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.error_profile_picture)
                    .into(profileImageView);
        }).addOnFailureListener(e -> Log.w(LOG_TAG, "Error getting download URL", e));
    }

    private void setupEditTextTransformations() {
        usernameTextView.setOnClickListener(v -> {
            usernameTextView.startAnimation(AnimationUtils.loadAnimation(
                    this, R.anim.slide_out_left));
            new Handler().postDelayed(() -> transformToEditText(usernameTextView,
                    R.id.usernameEditText), 10);
        });

        phoneNumberTextView.setOnClickListener(v -> {
            phoneNumberTextView.startAnimation(AnimationUtils.loadAnimation(
                    this, R.anim.slide_out_left));
            new Handler().postDelayed(() -> transformToEditText(phoneNumberTextView,
                    R.id.phoneNumberEditText), 10);
        });

        addressTextView.setOnClickListener(v -> {
            addressTextView.startAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.slide_out_left));
            new Handler().postDelayed(() -> transformToEditText(addressTextView,
                    R.id.addressEditText), 10);
        });
    }

    private void setupImagePicker() {
        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    selectedImageUri = uri;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ImageView imageView = findViewById(R.id.profileImageView);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "An exception occurred", e);
                    }
                });
    }

    private void transformToEditText(TextView textView, int newId) {
        EditText editText = createEditText(textView, newId);
        setEditTextProperties(editText);
        replaceTextViewWithEditText(textView, editText);
        animateAndFocusEditText(editText);
    }

    private EditText createEditText(TextView textView, int newId) {
        EditText editText = new EditText(this);
        editText.setLayoutParams(textView.getLayoutParams());
        editText.setId(newId);
        return editText;
    }

    private void setEditTextProperties(EditText editText) {
        int newId = editText.getId();
        if (newId == R.id.usernameEditText) {
            editText.setHint(R.string.username);
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        } else if (newId == R.id.phoneNumberEditText) {
            editText.setHint(R.string.phone_number);
            editText.setInputType(InputType.TYPE_CLASS_PHONE);
        } else if (newId == R.id.addressEditText) {
            editText.setHint(R.string.address);
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS |
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }

        editText.setPadding(10, 10, 10, 10);
        editText.setTextColor(Color.parseColor("#333333"));
        editText.setHintTextColor(Color.parseColor("#BF474747"));
        editText.setBackground(ContextCompat.getDrawable(this,
                R.drawable.edit_text_background));
        editText.setEms(10);
    }

    private void replaceTextViewWithEditText(TextView textView, EditText editText) {
        ViewGroup parent = (ViewGroup) textView.getParent();
        int index = parent.indexOfChild(textView);
        parent.removeView(textView);
        parent.addView(editText, index);
    }

    private void animateAndFocusEditText(EditText editText) {
        editText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        editText.requestFocus();
    }

    public void saveUserProfile(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        EditText usernameView = findViewById(R.id.usernameEditText);
        EditText phoneNumberView = findViewById(R.id.phoneNumberEditText);
        EditText addressView = findViewById(R.id.addressEditText);
        EditText passwordView = findViewById(R.id.editTextPassword);
        EditText passwordAgainView = findViewById(R.id.editTextPasswordAgain);

        String newPassword = getTextFromEditText(passwordView);
        String passwordAgain = getTextFromEditText(passwordAgainView);

        if (!newPassword.isEmpty() && areEditTextsNull(usernameView, phoneNumberView, addressView)) {
            updatePasswordIfMatch(newPassword, passwordAgain);
            return;
        }

        String newUsername = getTextFromEditText(usernameView);
        String newPhone = getTextFromEditText(phoneNumberView);
        String newAddress = getTextFromEditText(addressView);

        if (areFieldsEmpty(newUsername, newPhone, newAddress, newPassword)) {
            refreshActivity();
            return;
        }

        if (!newPassword.equals(passwordAgain)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        updateUserDetails(db, mAuth, storage, userId, newUsername, newPhone, newAddress, newPassword);
    }

    private String getTextFromEditText(EditText editText) {
        return editText != null ? editText.getText().toString() : "";
    }

    private boolean areEditTextsNull(EditText... editTexts) {
        for (EditText editText : editTexts) {
            if (editText != null) {
                return false;
            }
        }
        return true;
    }

    private void updatePasswordIfMatch(String newPassword, String passwordAgain) {
        if (!newPassword.equals(passwordAgain)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

//        mAuth.getCurrentUser().updatePassword(newPassword)
//                .addOnCompleteListener(passwordTask -> {
//                    if (passwordTask.isSuccessful()) {
//                        Log.d(LOG_TAG, "User password updated.");
//                        refreshActivity();
//                    } else {
//                        Log.w(LOG_TAG, "Error updating password", passwordTask.getException());
//                    }
//                });
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(passwordTask -> {
                        if (passwordTask.isSuccessful()) {
                            Log.d(LOG_TAG, "User password updated.");
                        } else {
                            Log.w(LOG_TAG, "Error updating password", passwordTask.getException());
                        }
                    });
        } else {
            Log.w(LOG_TAG, "No current user to update password for");
        }
    }

    private boolean areFieldsEmpty(String... fields) {
        for (String field : fields) {
            if (!field.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void updateUserDetails(FirebaseFirestore db, FirebaseAuth mAuth, FirebaseStorage storage,
                                   String userId, String newUsername, String newPhone,
                                   String newAddress, String newPassword) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = newUsername.isEmpty() ?
                            document.getString("username") : newUsername;
                    String phone = newPhone.isEmpty() ?
                            document.getString("phone") : newPhone;
                    String address = newAddress.isEmpty() ?
                            document.getString("address") : newAddress;

                    User user = new User(username, Objects.requireNonNull(mAuth.getCurrentUser()).
                            getEmail(), phone, address);

                    mAuth.getCurrentUser().updatePassword(newPassword)
                            .addOnCompleteListener(passwordTask -> {
                                if (passwordTask.isSuccessful()) {
                                    Log.d(LOG_TAG, "User password updated.");
                                } else {
                                    Log.w(LOG_TAG, "Error updating password",
                                            passwordTask.getException());
                                }
                            });

                    if (selectedImageUri != null) {
                        uploadImageAndUpdateUser(db, storage, userId, user);
                    } else {
                        updateUserInFirestore(db, userId, user);
                    }
                } else {
                    Log.d(LOG_TAG, "No such document");
                }
            } else {
                Log.d(LOG_TAG, "get failed with ", task.getException());
            }
        });
    }

    private void uploadImageAndUpdateUser(FirebaseFirestore db, FirebaseStorage storage,
                                          String userId, User user) {
        StorageReference storageRef = storage.getReference();
        StorageReference userImageRef = storageRef.child("images/" + userId + ".png");

        userImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    user.setProfilePicture(uri.toString());
                    updateUserInFirestore(db, userId, user);
                }))
                .addOnFailureListener(e -> Log.w(LOG_TAG,
                        "Error uploading image to Firebase Storage", e));
    }

    private void updateUserInFirestore(FirebaseFirestore db, String userId, User user) {
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(LOG_TAG, "User details updated in Firestore");
                    refreshActivity();
                })
                .addOnFailureListener(e -> Log.w(LOG_TAG,
                        "Error updating user details in Firestore", e));
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void changeProfilePicture(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        mGetContent.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to open gallery",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteUserProfile(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Delete user data from Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().
                            getCurrentUser()).getUid();
                    db.collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "User data deleted from Firestore"))
                            .addOnFailureListener(e -> Log.w(LOG_TAG, "Error deleting user data from Firestore", e));

                    // Delete user profile picture from Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference userImageRef = storage.getReference().child("images/" + userId + ".png");
                    userImageRef.delete()
                            .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "User profile picture deleted from Firebase Storage"))
                            .addOnFailureListener(e -> Log.w(LOG_TAG, "Error deleting user profile picture from Firebase Storage", e));

                    // Delete user from Firebase Authentication
                    FirebaseAuth.getInstance().getCurrentUser()
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(LOG_TAG, "User deleted from Firebase Authentication");
                                    // Redirect to login activity
                                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.w(LOG_TAG, "Error deleting user from Firebase Authentication", task.getException());
                                }
                            });
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void onBackPressed(View view) {
        Intent intent = new Intent(this, WebshopActivity.class);
        startActivity(intent);
        finish();
    }
}
