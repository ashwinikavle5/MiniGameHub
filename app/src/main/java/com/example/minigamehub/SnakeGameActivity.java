package com.example.minigamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SnakeGameActivity extends AppCompatActivity {

    private SnakeView snakeView;
    private TextView txtScore;
    private Button btnRestart;

    private DatabaseHelper databaseHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_snake_game);

        snakeView = findViewById(R.id.snakeView);
        txtScore = findViewById(R.id.txtScore);
        btnRestart = findViewById(R.id.btnRestart);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnUp = findViewById(R.id.btnUp);
        Button btnDown = findViewById(R.id.btnDown);
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);

        btnRestart.setVisibility(View.GONE);

        databaseHelper = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (snakeView != null) {
                    snakeView.stopGame();
                }
                finish();
            });
        }

        if (btnUp != null) {
            btnUp.setOnClickListener(v -> snakeView.changeDirection(1));
        }
        if (btnDown != null) {
            btnDown.setOnClickListener(v -> snakeView.changeDirection(3));
        }
        if (btnLeft != null) {
            btnLeft.setOnClickListener(v -> snakeView.changeDirection(2));
        }
        if (btnRight != null) {
            btnRight.setOnClickListener(v -> snakeView.changeDirection(0));
        }

        snakeView.setScoreListener(score -> {
            txtScore.setText("Score: " + score);
        });

        snakeView.setGameOverListener(score -> {

            if (username != null) {
                databaseHelper.saveScore(
                        username,
                        "Snake",
                        score
                );
            }

            btnRestart.setVisibility(
                    View.VISIBLE
            );
        });

        btnRestart.setOnClickListener(v -> {

            snakeView.restartGame();

            btnRestart.setVisibility(
                    View.GONE
            );
        });

        // AndroidX Back Button / Gesture
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (snakeView != null) {
                            snakeView.stopGame();
                        }

                        finish();
                    }
                }
        );
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (snakeView != null) {
            snakeView.stopGame();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();



        if (snakeView != null) {
            snakeView.startGame();
        }
    }
}