package com.example.studynotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    TextView txtEmail;
    Button btnLogout;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_profile,
                        container,
                        false
                );

        txtEmail =
                view.findViewById(R.id.txtEmail);

        btnLogout =
                view.findViewById(R.id.btnLogout);

        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        if (user != null) {

            txtEmail.setText(
                    user.getEmail()
            );
        }

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance()
                    .signOut();

            Intent intent =
                    new Intent(
                            getContext(),
                            LoginActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        return view;
    }
}