package com.example.studynotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            FirebaseAuth auth =
                    FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {

                auth.getCurrentUser()
                        .reload()
                        .addOnSuccessListener(unused -> {
                            startActivity(
                                    new Intent(
                                            SplashActivity.this,
                                            MainActivity.class
                                    )
                            );
                            finish();
                        })

                        .addOnFailureListener(e -> {
                            auth.signOut();
                            startActivity(
                                    new Intent(
                                            SplashActivity.this,
                                            LoginActivity.class
                                    )
                            );
                            finish();
                        });

            } else {
                startActivity(
                        new Intent(
                                SplashActivity.this,
                                LoginActivity.class
                        )
                );
                finish();
            }

        }, 2500);
    }
}