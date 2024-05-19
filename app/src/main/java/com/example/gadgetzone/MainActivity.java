package com.example.gadgetzone;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {
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

        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.send("Come check out the latest products in our store!");

        ImageView logoImageView = findViewById(R.id.logoImageView);
        Animation logoEntranceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_entrance_pop_in);
        logoImageView.startAnimation(logoEntranceAnimation);
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageView logoImageView = findViewById(R.id.logoImageView);
        Animation logoEntranceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_entrance_pop_in);

        logoImageView.setVisibility(View.VISIBLE);
        logoImageView.startAnimation(logoEntranceAnimation);
    }

    @Override
    protected void onPause() {
        super.onPause();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        ImageView logoImageView = findViewById(R.id.logoImageView);
        logoImageView.clearAnimation();
        logoImageView.setVisibility(View.INVISIBLE);
    }
}