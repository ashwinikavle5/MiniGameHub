package com.example.minigamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class FruitBlastActivity extends AppCompatActivity {

    private FruitBlastView fruitView;
    private TextView txtScore;
    private TextView txtLevel;
    private TextView txtStatus;
    private Button btnRestart;
    private Button btnNextLevel;
    private Button btnBack;

    private DatabaseHelper databaseHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_blast);

        fruitView = findViewById(R.id.fruitView);
        txtScore = findViewById(R.id.txtScore);
        txtLevel = findViewById(R.id.txtLevel);
        txtStatus = findViewById(R.id.txtStatus);
        btnRestart = findViewById(R.id.btnRestart);
        btnNextLevel = findViewById(R.id.btnNextLevel);
        btnBack = findViewById(R.id.btnBack);

        databaseHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");

        fruitView.setGameListener(new FruitBlastView.GameListener() {
            @Override
            public void onScoreChanged(int moves) {
                runOnUiThread(() -> {
                    txtScore.setText("MOVES: " + moves);
                });
            }

            @Override
            public void onLevelChanged(int level) {
                runOnUiThread(() -> {
                    txtLevel.setText("LEVEL " + level);
                    if (txtStatus != null) {
                        txtStatus.setText("Tap a tube to pick top ball, tap another to place");
                    }
                });
            }

            @Override
            public void onGameOver(int score) {
                runOnUiThread(() -> {
                    if (username != null && !username.trim().isEmpty()) {
                        // Save score under both names so it remains compatible with existing SQLite & ScoreBoard
                        databaseHelper.saveScore(username, "Sorting Balls", score);
                        databaseHelper.saveScore(username, "Fruit Blast", score);
                    }
                });
            }

            @Override
            public void onLevelCompleted(int moves, int level) {
                runOnUiThread(() -> {
                    if (btnNextLevel != null) {
                        btnNextLevel.setVisibility(View.VISIBLE);
                    }
                    if (txtStatus != null) {
                        txtStatus.setText("🎉 Level complete in " + moves + " moves!");
                    }
                });
            }
        });

        btnRestart.setOnClickListener(v -> {
            if (btnNextLevel != null) {
                btnNextLevel.setVisibility(View.GONE);
            }
            fruitView.restartGame();
        });

        if (btnNextLevel != null) {
            btnNextLevel.setOnClickListener(v -> {
                btnNextLevel.setVisibility(View.GONE);
                fruitView.nextLevel();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (fruitView != null) {
                    fruitView.stopGame();
                }
                finish();
            });
        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (fruitView != null) {
                            fruitView.stopGame();
                        }
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fruitView != null) {
            fruitView.stopGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fruitView != null) {
            fruitView.startGame();
        }
    }
}
