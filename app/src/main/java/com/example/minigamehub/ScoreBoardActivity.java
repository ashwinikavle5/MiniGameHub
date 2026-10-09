package com.example.minigamehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoreBoardActivity extends AppCompatActivity {

    TextView txtSnakeScore;
    TextView txtTicTacToeScore;
    TextView txtTileEscapeScore;
    TextView txtBubbleShooterScore;
    TextView txtFruitBlastScore;

    Button btnBack;

    DatabaseHelper databaseHelper;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_score_board
        );

        // FIND VIEWS

        txtSnakeScore =
                findViewById(R.id.txtSnakeScore);

        txtTicTacToeScore =
                findViewById(R.id.txtTicTacToeScore);

        txtTileEscapeScore =
                findViewById(R.id.txtTileEscapeScore);

        txtBubbleShooterScore =
                findViewById(R.id.txtBubbleShooterScore);

        txtFruitBlastScore =
                findViewById(R.id.txtFruitBlastScore);

        btnBack =
                findViewById(R.id.btnBack);

        databaseHelper =
                new DatabaseHelper(this);

        username =
                getIntent().getStringExtra(
                        "username"
                );

        loadScores();

        btnBack.setOnClickListener(v -> {

            finish();

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScores();
    }

    private void loadScores() {
        String queryUser = (username != null && !username.trim().isEmpty()) ? username : "Player";

        int snakeScore =
                databaseHelper.getBestScore(
                        queryUser,
                        "Snake"
                );

        int ticTacToeScore =
                databaseHelper.getBestScore(
                        queryUser,
                        "Tic Tac Toe"
                );

        int subwayRunnerScore =
                Math.max(
                        databaseHelper.getBestScore(queryUser, "Subway Runner"),
                        databaseHelper.getBestScore(queryUser, "Tile Escape")
                );

        int bubbleShooterScore =
                databaseHelper.getBestScore(
                        queryUser,
                        "Bubble Shooter"
                );

        int fruitBlastScore =
                databaseHelper.getBestScore(
                        queryUser,
                        "Fruit Blast"
                );

        txtSnakeScore.setText(
                "Best Score: " + snakeScore
        );

        txtTicTacToeScore.setText(
                "Best Score: " + ticTacToeScore
        );

        txtTileEscapeScore.setText(
                "Best Score: " + subwayRunnerScore
        );

        txtBubbleShooterScore.setText(
                "Best Score: " + bubbleShooterScore
        );

        txtFruitBlastScore.setText(
                "Best Score: " + fruitBlastScore
        );
    }
}
