package com.example.minigamehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText edtSignupName;
    EditText edtSignupPassword;
    EditText edtConfirmPassword;

    Button btnSignup;
    TextView txtLogin;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        edtSignupName =
                findViewById(R.id.edtSignupName);

        edtSignupPassword =
                findViewById(R.id.edtSignupPassword);

        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        btnSignup =
                findViewById(R.id.btnSignup);

        txtLogin =
                findViewById(R.id.txtLogin);

        databaseHelper =
                new DatabaseHelper(this);

        // CREATE ACCOUNT
        btnSignup.setOnClickListener(v -> {

            String name =
                    edtSignupName.getText().toString().trim();

            String password =
                    edtSignupPassword.getText().toString().trim();

            String confirmPassword =
                    edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {

                edtSignupName.setError(
                        "Enter your name"
                );

                edtSignupName.requestFocus();
                return;
            }

            if (password.isEmpty()) {

                edtSignupPassword.setError(
                        "Enter password"
                );

                edtSignupPassword.requestFocus();
                return;
            }

            if (password.length() < 4) {

                edtSignupPassword.setError(
                        "Password must contain at least 4 characters"
                );

                edtSignupPassword.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {

                edtConfirmPassword.setError(
                        "Confirm your password"
                );

                edtConfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {

                edtConfirmPassword.setError(
                        "Passwords do not match"
                );

                edtConfirmPassword.requestFocus();
                return;
            }

            if (databaseHelper.userExists(name)) {

                Toast.makeText(
                        SignUpActivity.this,
                        "User already exists!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            boolean inserted =
                    databaseHelper.addUser(
                            name,
                            password
                    );

            if (inserted) {

                Toast.makeText(
                        SignUpActivity.this,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent =
                        new Intent(
                                SignUpActivity.this,
                                MainActivity.class
                        );

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        SignUpActivity.this,
                        "Registration failed!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // BACK TO LOGIN
        txtLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SignUpActivity.this,
                            MainActivity.class
                    );

            startActivity(intent);
            finish();
        });
    }
}