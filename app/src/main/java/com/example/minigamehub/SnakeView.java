package com.example.minigamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<int[]> snake = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int foodX;
    private int foodY;

    // 0 = RIGHT
    // 1 = UP
    // 2 = LEFT
    // 3 = DOWN
    private int direction = 0;
    private int nextDirection = 0;

    private int score = 0;

    private boolean gameOver = false;
    private boolean running = false;

    private final int gridSize = 20;

    private float cellSize;

    private ScoreListener scoreListener;
    private GameOverListener gameOverListener;

    private final Runnable gameLoop = new Runnable() {

        @Override
        public void run() {

            if (!running || gameOver) {
                return;
            }

            moveSnake();

            invalidate();

            if (!gameOver) {
                handler.postDelayed(
                        this,
                        150
                );
            }
        }
    };

    // SCORE LISTENER
    public interface ScoreListener {
        void onScoreChanged(int score);
    }

    // GAME OVER LISTENER
    public interface GameOverListener {
        void onGameOver(int score);
    }

    public SnakeView(Context context) {
        super(context);
        init();
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        setBackgroundColor(Color.rgb(20, 30, 25));
        restartGame();
    }

    public void setScoreListener(
            ScoreListener listener) {

        this.scoreListener = listener;
    }

    public void setGameOverListener(
            GameOverListener listener) {

        this.gameOverListener = listener;
    }

    // START GAME
    public void startGame() {

        if (gameOver) {
            return;
        }

        if (!running) {

            running = true;

            handler.removeCallbacks(
                    gameLoop
            );

            handler.postDelayed(
                    gameLoop,
                    150
            );
        }
    }

    // STOP GAME
    public void stopGame() {

        running = false;

        handler.removeCallbacks(
                gameLoop
        );
    }

    // RESTART GAME
    public void restartGame() {

        handler.removeCallbacks(
                gameLoop
        );

        snake.clear();

        // Starting snake
        snake.add(
                new int[]{10, 10}
        );

        snake.add(
                new int[]{9, 10}
        );

        snake.add(
                new int[]{8, 10}
        );

        direction = 0;
        nextDirection = 0;

        score = 0;

        gameOver = false;
        running = true;

        createFood();

        if (scoreListener != null) {

            scoreListener.onScoreChanged(
                    score
            );
        }

        handler.postDelayed(
                gameLoop,
                150
        );

        invalidate();
    }

    // CREATE FOOD
    private void createFood() {

        do {

            foodX =
                    random.nextInt(
                            gridSize
                    );

            foodY =
                    random.nextInt(
                            gridSize
                    );

            boolean onSnake = false;

            for (int[] part : snake) {

                if (part[0] == foodX &&
                        part[1] == foodY) {

                    onSnake = true;

                    break;
                }
            }

            if (!onSnake) {
                break;
            }

        } while (true);
    }

    // MOVE SNAKE
    private void moveSnake() {

        direction = nextDirection;

        int[] head =
                snake.get(0);

        int newX =
                head[0];

        int newY =
                head[1];

        if (direction == 0) {

            // RIGHT
            newX++;

        } else if (direction == 1) {

            // UP
            newY--;

        } else if (direction == 2) {

            // LEFT
            newX--;

        } else {

            // DOWN
            newY++;
        }

        // WALL COLLISION
        if (newX < 0 ||
                newX >= gridSize ||
                newY < 0 ||
                newY >= gridSize) {

            endGame();

            return;
        }

        // SELF COLLISION
        for (int i = 0;
             i < snake.size() - 1;
             i++) {

            int[] part =
                    snake.get(i);

            if (part[0] == newX &&
                    part[1] == newY) {

                endGame();

                return;
            }
        }

        // ADD NEW HEAD
        snake.add(
                0,
                new int[]{
                        newX,
                        newY
                }
        );

        // FOOD EATEN
        if (newX == foodX &&
                newY == foodY) {

            score++;

            if (scoreListener != null) {

                scoreListener.onScoreChanged(
                        score
                );
            }

            createFood();

        } else {

            // REMOVE TAIL
            snake.remove(
                    snake.size() - 1
            );
        }
    }

    // GAME OVER
    private void endGame() {

        if (gameOver) {
            return;
        }

        gameOver = true;
        running = false;

        handler.removeCallbacks(
                gameLoop
        );

        if (gameOverListener != null) {

            gameOverListener.onGameOver(
                    score
            );
        }

        invalidate();
    }

    // DRAW GAME
    @Override
    protected void onDraw(
            Canvas canvas) {

        super.onDraw(canvas);

        if (getWidth() <= 0 ||
                getHeight() <= 0) {

            return;
        }

        cellSize =
                Math.min(
                        getWidth(),
                        getHeight()
                ) / (float) gridSize;

        // FOOD
        paint.setColor(
                Color.RED
        );

        canvas.drawCircle(

                foodX * cellSize +
                        cellSize / 2,

                foodY * cellSize +
                        cellSize / 2,

                cellSize * 0.35f,

                paint
        );

        // SNAKE
        for (int i = 0;
             i < snake.size();
             i++) {

            int[] part =
                    snake.get(i);

            if (i == 0) {

                // HEAD
                paint.setColor(
                        Color.rgb(
                                120,
                                230,
                                100
                        )
                );

            } else {

                // BODY
                paint.setColor(
                        Color.rgb(
                                70,
                                200,
                                80
                        )
                );
            }

            float left =
                    part[0] *
                            cellSize +
                            2;

            float top =
                    part[1] *
                            cellSize +
                            2;

            float right =
                    (part[0] + 1) *
                            cellSize -
                            2;

            float bottom =
                    (part[1] + 1) *
                            cellSize -
                            2;

            canvas.drawRoundRect(

                    new RectF(
                            left,
                            top,
                            right,
                            bottom
                    ),

                    8,
                    8,

                    paint
            );
        }

        // GAME OVER
        if (gameOver) {

            paint.setColor(
                    Color.argb(
                            180,
                            0,
                            0,
                            0
                    )
            );

            canvas.drawRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    paint
            );

            paint.setColor(
                    Color.WHITE
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(Math.max(40, getWidth() * 0.08f));

            canvas.drawText(
                    "GAME OVER",
                    getWidth() / 2f,
                    getHeight() / 2f - 10,
                    paint
            );

            paint.setTextSize(Math.max(22, getWidth() * 0.045f));

            canvas.drawText(
                    "Score: " + score,
                    getWidth() / 2f,
                    getHeight() / 2f + 40,
                    paint
            );
        }
    }

    public void changeDirection(int newDir) {
        if (gameOver) {
            return;
        }
        if (newDir == 0 && direction != 2) {
            nextDirection = 0; // RIGHT
        } else if (newDir == 1 && direction != 3) {
            nextDirection = 1; // UP
        } else if (newDir == 2 && direction != 0) {
            nextDirection = 2; // LEFT
        } else if (newDir == 3 && direction != 1) {
            nextDirection = 3; // DOWN
        }
    }

    private float touchStartX = 0;
    private float touchStartY = 0;

    // TOUCH CONTROLS (Swipe & Tap)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                float minSwipeDistance = 35f;

                if (Math.abs(dx) > minSwipeDistance || Math.abs(dy) > minSwipeDistance) {
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) {
                            changeDirection(0); // RIGHT
                        } else {
                            changeDirection(2); // LEFT
                        }
                    } else {
                        if (dy > 0) {
                            changeDirection(3); // DOWN
                        } else {
                            changeDirection(1); // UP
                        }
                    }
                } else {
                    float centerX = getWidth() / 2f;
                    float centerY = getHeight() / 2f;
                    float tapX = event.getX();
                    float tapY = event.getY();

                    if (Math.abs(tapX - centerX) > Math.abs(tapY - centerY)) {
                        if (tapX > centerX) {
                            changeDirection(0);
                        } else {
                            changeDirection(2);
                        }
                    } else {
                        if (tapY > centerY) {
                            changeDirection(3);
                        } else {
                            changeDirection(1);
                        }
                    }
                }
                return true;
        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(gameLoop);
    }
}
