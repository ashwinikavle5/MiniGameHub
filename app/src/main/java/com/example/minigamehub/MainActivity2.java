package com.example.minigamehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    TextView txtWelcome;

    LinearLayout cardSnake;
    LinearLayout cardTicTacToe;
    LinearLayout cardTileEscape;
    LinearLayout cardBubbleShooter;
    LinearLayout cardFruitBlast;
    LinearLayout cardScoreBoard;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_menu);

        txtWelcome = findViewById(R.id.txtWelcome);

        cardSnake = findViewById(R.id.cardSnake);
        cardTicTacToe = findViewById(R.id.cardTicTacToe);
        cardTileEscape = findViewById(R.id.cardTileEscape);
        cardBubbleShooter = findViewById(R.id.cardBubbleShooter);
        cardFruitBlast = findViewById(R.id.cardFruitBlast);
        cardScoreBoard = findViewById(R.id.cardScoreBoard);

        username =
                getIntent().getStringExtra("username");

        if (username != null) {

            txtWelcome.setText(
                    "Welcome, " + username + "!"
            );
        }


        // SNAKE
        cardSnake.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    SnakeGameActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });


        // TIC TAC TOE
        cardTicTacToe.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    TicTacToeActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });


        // TILE ESCAPE
        cardTileEscape.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    TileEscapeActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });


        // BUBBLE SHOOTER
        cardBubbleShooter.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    BubbleShooterActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });


        // FRUIT BLAST
        cardFruitBlast.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    FruitBlastActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });


        // SCORE BOARD
        cardScoreBoard.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity2.this,
                    ScoreBoardActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            startActivity(intent);
        });
    }
}