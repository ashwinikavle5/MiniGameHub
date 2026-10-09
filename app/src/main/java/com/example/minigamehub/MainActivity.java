package com.example.minigamehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class
MainActivity extends AppCompatActivity {

    EditText edtName;
    EditText edtPassword;

    Button btnPlay;
    TextView txtSignup;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);

        btnPlay = findViewById(R.id.btnPlay);
        txtSignup = findViewById(R.id.txtSignup);

        databaseHelper = new DatabaseHelper(this);

        // LOGIN
        btnPlay.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty()) {
                edtName.setError("Enter your name");
                edtName.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Enter your password");
                edtPassword.requestFocus();
                return;
            }

            boolean loginSuccess =
                    databaseHelper.checkUser(name, password);

            if (loginSuccess) {

                Toast.makeText(
                        MainActivity.this,
                        "Login Successful!",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        MainActivity.this,
                        MainActivity2.class
                );

                intent.putExtra("username", name);

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Invalid name or password!",9.
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // SIGN UP
        txtSignup.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    SignUpActivity.class
            );

            startActivity(intent);
        });
    }
}
