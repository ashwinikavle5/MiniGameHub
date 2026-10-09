package com.example.minigamehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TicTacToeActivity extends AppCompatActivity {

    Button[][] buttons = new Button[3][3];

    TextView txtStatus;
    Button btnRestart;

    boolean playerXTurn = true;
    int moves = 0;
    boolean gameFinished = false;

    DatabaseHelper databaseHelper;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tic_tac_toe);

        txtStatus = findViewById(R.id.txtStatus);
        btnRestart = findViewById(R.id.btnRestart);

        buttons[0][0] = findViewById(R.id.btn00);
        buttons[0][1] = findViewById(R.id.btn01);
        buttons[0][2] = findViewById(R.id.btn02);

        buttons[1][0] = findViewById(R.id.btn10);
        buttons[1][1] = findViewById(R.id.btn11);
        buttons[1][2] = findViewById(R.id.btn12);

        buttons[2][0] = findViewById(R.id.btn20);
        buttons[2][1] = findViewById(R.id.btn21);
        buttons[2][2] = findViewById(R.id.btn22);

        databaseHelper = new DatabaseHelper(this);

        username = getIntent().getStringExtra("username");

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                final int r = row;
                final int c = col;

                buttons[row][col].setOnClickListener(v -> {
                    playMove(r, c);
                });
            }
        }

        btnRestart.setOnClickListener(v -> {
            restartGame();
        });

        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void playMove(int row, int col) {

        if (gameFinished) {
            return;
        }

        Button button = buttons[row][col];

        if (!button.getText().toString().isEmpty()) {
            return;
        }

        if (playerXTurn) {
            button.setText("X");
        } else {
            button.setText("O");
        }

        moves++;

        if (checkWinner()) {

            gameFinished = true;

            if (playerXTurn) {

                txtStatus.setText("🎉 Player X Wins!");

                saveScore(100);

            } else {

                txtStatus.setText("🎉 Player O Wins!");

                saveScore(0);
            }

            disableBoard();

            return;
        }

        if (moves == 9) {

            gameFinished = true;

            txtStatus.setText("🤝 It's a Draw!");

            saveScore(50);

            return;
        }

        playerXTurn = !playerXTurn;

        if (playerXTurn) {
            txtStatus.setText("Player X's Turn");
        } else {
            txtStatus.setText("Player O's Turn");
        }
    }

    private void saveScore(int score) {

        if (username != null) {

            databaseHelper.saveScore(
                    username,
                    "Tic Tac Toe",
                    score
            );
        }
    }

    private boolean checkWinner() {

        for (int i = 0; i < 3; i++) {

            if (same(
                    buttons[i][0],
                    buttons[i][1],
                    buttons[i][2]
            )) {
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {

            if (same(
                    buttons[0][i],
                    buttons[1][i],
                    buttons[2][i]
            )) {
                return true;
            }
        }

        if (same(
                buttons[0][0],
                buttons[1][1],
                buttons[2][2]
        )) {
            return true;
        }

        if (same(
                buttons[0][2],
                buttons[1][1],
                buttons[2][0]
        )) {
            return true;
        }

        return false;
    }

    private boolean same(
            Button a,
            Button b,
            Button c) {

        String first = a.getText().toString();

        if (first.isEmpty()) {
            return false;
        }

        return first.equals(
                b.getText().toString()
        ) && first.equals(
                c.getText().toString()
        );
    }

    private void disableBoard() {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                buttons[row][col].setEnabled(false);
            }
        }
    }

    private void restartGame() {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < 3; col++) {

                buttons[row][col].setText("");
                buttons[row][col].setEnabled(true);
            }
        }

        playerXTurn = true;
        moves = 0;
        gameFinished = false;

        txtStatus.setText("Player X's Turn");
    }
}
