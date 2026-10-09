package com.example.minigamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class BubbleShooterActivity extends AppCompatActivity {

    BubbleShooterView bubbleView;

    TextView txtScore;
    TextView txtLevel;

    Button btnRestart;

    DatabaseHelper databaseHelper;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bubble_shooter);

        bubbleView = findViewById(R.id.bubbleView);
        txtScore = findViewById(R.id.txtScore);
        txtLevel = findViewById(R.id.txtLevel);
        btnRestart = findViewById(R.id.btnRestart);

        databaseHelper = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");

        bubbleView.setGameListener(
                new BubbleShooterView.GameListener() {

                    @Override
                    public void onScoreChanged(int score) {
                        txtScore.setText("⭐ " + score);
                    }

                    @Override
                    public void onLevelChanged(int level) {
                        txtLevel.setText("LEVEL " + level);
                    }

                    @Override
                    public void onGameOver(int score) {

                        if (username != null) {
                            databaseHelper.saveScore(
                                    username,
                                    "Bubble Shooter",
                                    score
                            );
                        }

                        btnRestart.setVisibility(
                                View.VISIBLE
                        );
                    }
                }
        );

        btnRestart.setOnClickListener(v -> {

            bubbleView.restartGame();

            btnRestart.setVisibility(
                    View.GONE
            );
        });

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (bubbleView != null) {
                    bubbleView.stopGame();
                }
                finish();
            });
        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (bubbleView != null) {
                            bubbleView.stopGame();
                        }

                        finish();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bubbleView != null) {
            bubbleView.stopGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bubbleView != null) {
            bubbleView.startGame();
        }
    }
}