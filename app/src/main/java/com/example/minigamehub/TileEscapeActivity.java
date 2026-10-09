package com.example.minigamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class TileEscapeActivity extends AppCompatActivity {

    private TextView txtScore;
    private TextView txtDistance;
    private TextView txtCoins;
    private Button btnRestart;
    private Button btnBack;
    private Button btnLeft;
    private Button btnJump;
    private Button btnSlide;
    private Button btnRight;
    private FrameLayout gameContainer;

    private SubwayRunnerView runnerView;
    private DatabaseHelper databaseHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_escape);

        txtScore = findViewById(R.id.txtScore);
        txtDistance = findViewById(R.id.txtDistance);
        txtCoins = findViewById(R.id.txtCoins);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);
        btnLeft = findViewById(R.id.btnLeft);
        btnJump = findViewById(R.id.btnJump);
        btnSlide = findViewById(R.id.btnSlide);
        btnRight = findViewById(R.id.btnRight);
        gameContainer = findViewById(R.id.gameContainer);

        databaseHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");

        runnerView = new SubwayRunnerView(this);
        gameContainer.addView(runnerView);

        runnerView.setListener(new SubwayRunnerView.RunnerListener() {
            @Override
            public void onStatsUpdated(int score, int distance, int coins) {
                runOnUiThread(() -> {
                    txtScore.setText("⭐ SCORE: " + score);
                    txtDistance.setText("DIST: " + distance + "m");
                    txtCoins.setText("🪙 " + coins);
                });
            }

            @Override
            public void onGameOver(int finalScore, int finalDistance, int finalCoins) {
                runOnUiThread(() -> {
                    if (username != null && !username.trim().isEmpty()) {
                        databaseHelper.saveScore(username, "Subway Runner", finalScore);
                    }
                    btnRestart.setVisibility(View.VISIBLE);
                });
            }
        });

        btnRestart.setOnClickListener(v -> {
            btnRestart.setVisibility(View.GONE);
            runnerView.restart();
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (runnerView != null) {
                    runnerView.stop();
                }
                finish();
            });
        }

        if (btnLeft != null) {
            btnLeft.setOnClickListener(v -> runnerView.moveLeft());
        }
        if (btnRight != null) {
            btnRight.setOnClickListener(v -> runnerView.moveRight());
        }
        if (btnJump != null) {
            btnJump.setOnClickListener(v -> runnerView.jump());
        }
        if (btnSlide != null) {
            btnSlide.setOnClickListener(v -> runnerView.slide());
        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (runnerView != null) {
                            runnerView.stop();
                        }
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (runnerView != null) {
            runnerView.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (runnerView != null) {
            runnerView.start();
        }
    }

    // =========================================================================
    // SUBWAY RUNNER VIEW (3-LANE ENDLESS RUNNER)
    // =========================================================================
    public static class SubwayRunnerView extends View {

        public interface RunnerListener {
            void onStatsUpdated(int score, int distance, int coins);
            void onGameOver(int finalScore, int finalDistance, int finalCoins);
        }

        private static final int TYPE_HURDLE = 0;      // Low hurdle: jump or switch lane
        private static final int TYPE_BARRIER = 1;     // High barrier: slide or switch lane
        private static final int TYPE_TRAIN = 2;       // Subway train: must switch lane

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private RunnerListener listener;

        // Player state
        private int currentLane = 1; // 0 = Left, 1 = Center, 2 = Right
        private float playerCurrentX = 0f;
        private boolean isJumping = false;
        private float jumpTime = 0f;
        private final float jumpDuration = 0.65f;
        private boolean isSliding = false;
        private float slideTime = 0f;
        private final float slideDuration = 0.65f;

        // Game stats & loop
        private boolean running = false;
        private boolean gameOver = false;
        private float speed = 500f; // pixels per second
        private float distance = 0f;
        private int coins = 0;
        private long lastFrameTime = 0;

        // Visual scrolling
        private float trackOffset = 0f;
        private float spawnTimer = 0f;

        // Objects on track
        private final ArrayList<Obstacle> obstacles = new ArrayList<>();
        private final ArrayList<Coin> coinList = new ArrayList<>();

        // Touch tracking
        private float touchDownX = 0f;
        private float touchDownY = 0f;

        public SubwayRunnerView(Context context) {
            super(context);
            restart();
        }

        public void setListener(RunnerListener listener) {
            this.listener = listener;
            if (listener != null) {
                listener.onStatsUpdated(calculateScore(), (int) distance, coins);
            }
        }

        private int calculateScore() {
            return (int) distance + (coins * 10);
        }

        public void start() {
            if (gameOver) {
                return;
            }
            if (!running) {
                running = true;
                lastFrameTime = System.currentTimeMillis();
                handler.removeCallbacks(gameLoop);
                handler.post(gameLoop);
            }
        }

        public void stop() {
            running = false;
            handler.removeCallbacks(gameLoop);
        }

        public void restart() {
            handler.removeCallbacks(gameLoop);
            currentLane = 1;
            playerCurrentX = 0f;
            isJumping = false;
            jumpTime = 0f;
            isSliding = false;
            slideTime = 0f;

            speed = 520f;
            distance = 0f;
            coins = 0;
            trackOffset = 0f;
            spawnTimer = 0f;
            obstacles.clear();
            coinList.clear();

            gameOver = false;
            running = true;
            lastFrameTime = System.currentTimeMillis();

            if (listener != null) {
                listener.onStatsUpdated(0, 0, 0);
            }

            handler.postDelayed(gameLoop, 150);
            invalidate();
        }

        public void moveLeft() {
            if (gameOver || !running) return;
            if (currentLane > 0) {
                currentLane--;
            }
        }

        public void moveRight() {
            if (gameOver || !running) return;
            if (currentLane < 2) {
                currentLane++;
            }
        }

        public void jump() {
            if (gameOver || !running) return;
            if (!isJumping && !isSliding) {
                isJumping = true;
                jumpTime = 0f;
            }
        }

        public void slide() {
            if (gameOver || !running) return;
            if (!isSliding) {
                if (isJumping) {
                    isJumping = false; // Fast dive into slide
                }
                isSliding = true;
                slideTime = 0f;
            }
        }

        private final Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!running || gameOver) {
                    return;
                }
                long now = System.currentTimeMillis();
                float dt = (now - lastFrameTime) / 1000f;
                lastFrameTime = now;
                if (dt > 0.05f) {
                    dt = 0.05f;
                }

                updateGame(dt);
                invalidate();

                if (!gameOver) {
                    handler.postDelayed(this, 16); // ~60fps
                }
            }
        };

        private void updateGame(float dt) {
            float width = getWidth();
            float height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            float laneWidth = width / 3f;
            float targetX = laneWidth * (currentLane + 0.5f);
            if (playerCurrentX == 0f) {
                playerCurrentX = targetX;
            } else {
                playerCurrentX += (targetX - playerCurrentX) * Math.min(1f, dt * 14f);
            }

            // Update Jump
            if (isJumping) {
                jumpTime += dt;
                if (jumpTime >= jumpDuration) {
                    isJumping = false;
                    jumpTime = 0f;
                }
            }

            // Update Slide
            if (isSliding) {
                slideTime += dt;
                if (slideTime >= slideDuration) {
                    isSliding = false;
                    slideTime = 0f;
                }
            }

            // Increase distance & speed smoothly
            distance += (speed * dt * 0.08f);
            speed = Math.min(1050f, 520f + (distance * 0.45f));

            trackOffset = (trackOffset + speed * dt) % 90f;

            // Move obstacles
            Iterator<Obstacle> obsIter = obstacles.iterator();
            while (obsIter.hasNext()) {
                Obstacle obs = obsIter.next();
                obs.y += speed * dt;
                if (obs.y > height + 200) {
                    obsIter.remove();
                }
            }

            // Move coins
            Iterator<Coin> coinIter = coinList.iterator();
            while (coinIter.hasNext()) {
                Coin coin = coinIter.next();
                coin.y += speed * dt;
                if (coin.y > height + 100) {
                    coinIter.remove();
                }
            }

            // Spawning
            spawnTimer += dt;
            float spawnInterval = Math.max(1.0f, 2.2f - (distance / 400f));
            if (spawnTimer >= spawnInterval) {
                spawnTimer = 0f;
                spawnPattern(laneWidth);
            }

            // Check Coin Collection
            float playerBaseY = height * 0.78f;
            float playerRadius = laneWidth * 0.28f;

            coinIter = coinList.iterator();
            while (coinIter.hasNext()) {
                Coin coin = coinIter.next();
                if (coin.lane == currentLane) {
                    float dy = Math.abs(coin.y - playerBaseY);
                    if (dy < playerRadius * 1.6f) {
                        coins++;
                        coinIter.remove();
                    }
                }
            }

            // Check Obstacle Collisions
            for (Obstacle obs : obstacles) {
                if (obs.lane == currentLane) {
                    float obsTop = obs.y;
                    float obsBottom = obs.y + obs.height;

                    // Check Y overlap with player
                    if (obsBottom > (playerBaseY - playerRadius * 0.7f) &&
                            obsTop < (playerBaseY + playerRadius * 0.7f)) {

                        if (obs.type == TYPE_HURDLE) {
                            // Safe if jumping high enough
                            float jumpProgress = jumpTime / jumpDuration;
                            if (isJumping && jumpProgress > 0.15f && jumpProgress < 0.85f) {
                                // Jumped over safely
                                continue;
                            }
                            triggerGameOver();
                            return;
                        } else if (obs.type == TYPE_BARRIER) {
                            // Safe if sliding underneath
                            if (isSliding) {
                                continue;
                            }
                            triggerGameOver();
                            return;
                        } else if (obs.type == TYPE_TRAIN) {
                            // Train cannot be avoided except by lane change
                            triggerGameOver();
                            return;
                        }
                    }
                }
            }

            if (listener != null) {
                listener.onStatsUpdated(calculateScore(), (int) distance, coins);
            }
        }

        private void spawnPattern(float laneWidth) {
            int blockedLane1 = random.nextInt(3);
            int blockedLane2 = -1;
            // Sometimes block 2 lanes (never 3, always leave at least 1 open lane)
            if (random.nextBoolean() && distance > 100) {
                blockedLane2 = (blockedLane1 + 1 + random.nextInt(2)) % 3;
            }

            // Obstacle 1
            int type1 = random.nextInt(3); // 0=hurdle, 1=barrier, 2=train
            Obstacle o1 = new Obstacle();
            o1.lane = blockedLane1;
            o1.type = type1;
            o1.y = -220f;
            o1.height = (type1 == TYPE_TRAIN) ? (laneWidth * 1.5f) : (laneWidth * 0.5f);
            obstacles.add(o1);

            // Obstacle 2 if applicable
            if (blockedLane2 != -1) {
                int type2 = random.nextInt(2); // Hurdle or barrier
                Obstacle o2 = new Obstacle();
                o2.lane = blockedLane2;
                o2.type = type2;
                o2.y = -220f;
                o2.height = laneWidth * 0.5f;
                obstacles.add(o2);
            }

            // Spawn Coins in open lanes
            for (int lane = 0; lane < 3; lane++) {
                if (lane != blockedLane1 && lane != blockedLane2) {
                    if (random.nextInt(100) < 65) {
                        for (int k = 0; k < 3; k++) {
                            Coin c = new Coin();
                            c.lane = lane;
                            c.y = -220f - (k * 70f);
                            coinList.add(c);
                        }
                    }
                }
            }
        }

        private void triggerGameOver() {
            if (gameOver) return;
            gameOver = true;
            running = false;
            handler.removeCallbacks(gameLoop);

            int finalScore = calculateScore();
            if (listener != null) {
                listener.onGameOver(finalScore, (int) distance, coins);
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            if (width <= 0 || height <= 0) return;

            float laneWidth = width / 3f;

            // 1. Draw Subway Track Ground
            paint.setColor(Color.rgb(20, 24, 33));
            canvas.drawRect(0, 0, width, height, paint);

            // 2. Draw Track Ties (sleepers moving down)
            paint.setColor(Color.rgb(38, 44, 58));
            paint.setStrokeWidth(12f);
            for (float y = trackOffset - 90; y < height; y += 70) {
                canvas.drawLine(10, y, width - 10, y, paint);
            }

            // 3. Draw Steel Rails
            paint.setColor(Color.rgb(144, 164, 174));
            paint.setStrokeWidth(5f);
            for (int i = 0; i < 3; i++) {
                float laneCenter = laneWidth * (i + 0.5f);
                float railOffset = laneWidth * 0.32f;
                canvas.drawLine(laneCenter - railOffset, 0, laneCenter - railOffset, height, paint);
                canvas.drawLine(laneCenter + railOffset, 0, laneCenter + railOffset, height, paint);
            }

            // 4. Draw Lane Dividers
            paint.setColor(Color.argb(80, 255, 255, 255));
            paint.setStrokeWidth(3f);
            canvas.drawLine(laneWidth, 0, laneWidth, height, paint);
            canvas.drawLine(laneWidth * 2, 0, laneWidth * 2, height, paint);

            // 5. Draw Borders / Tunnel Walls
            paint.setColor(Color.rgb(255, 179, 0));
            paint.setStrokeWidth(8f);
            canvas.drawLine(4, 0, 4, height, paint);
            canvas.drawLine(width - 4, 0, width - 4, height, paint);

            // 6. Draw Coins
            for (Coin coin : coinList) {
                float cx = laneWidth * (coin.lane + 0.5f);
                float cy = coin.y;
                if (cy > -50 && cy < height + 50) {
                    paint.setColor(Color.rgb(255, 215, 0));
                    canvas.drawCircle(cx, cy, 18f, paint);

                    paint.setColor(Color.rgb(255, 245, 157));
                    canvas.drawCircle(cx, cy, 12f, paint);

                    paint.setColor(Color.rgb(230, 81, 0));
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(16f);
                    canvas.drawText("★", cx, cy + 5f, paint);
                }
            }

            // 7. Draw Obstacles
            for (Obstacle obs : obstacles) {
                float cx = laneWidth * (obs.lane + 0.5f);
                float left = cx - laneWidth * 0.40f;
                float right = cx + laneWidth * 0.40f;
                float top = obs.y;
                float bottom = obs.y + obs.height;

                if (bottom < -50 || top > height + 50) continue;

                if (obs.type == TYPE_HURDLE) {
                    // Low Hurdle (Yellow/Red hazard stripes)
                    paint.setColor(Color.rgb(211, 47, 47));
                    canvas.drawRoundRect(new RectF(left, top, right, bottom), 8f, 8f, paint);

                    paint.setColor(Color.rgb(255, 235, 59));
                    paint.setStrokeWidth(6f);
                    canvas.drawLine(left + 8, top + 6, right - 8, top + 6, paint);
                    canvas.drawLine(left + 8, bottom - 6, right - 8, bottom - 6, paint);

                    paint.setColor(Color.WHITE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(Math.max(14f, laneWidth * 0.16f));
                    canvas.drawText("🚧 JUMP", cx, top + obs.height * 0.65f, paint);

                } else if (obs.type == TYPE_BARRIER) {
                    // High Overhead Sign (Slide under)
                    paint.setColor(Color.rgb(245, 124, 0));
                    canvas.drawRoundRect(new RectF(left, top, right, bottom), 10f, 10f, paint);

                    // Caution stripes
                    paint.setColor(Color.rgb(33, 33, 33));
                    paint.setStrokeWidth(5f);
                    canvas.drawLine(left + 10, top + 8, right - 10, top + 8, paint);

                    paint.setColor(Color.WHITE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(Math.max(14f, laneWidth * 0.16f));
                    canvas.drawText("⚠️ SLIDE", cx, top + obs.height * 0.65f, paint);

                } else {
                    // Train / Blocker
                    paint.setColor(Color.rgb(41, 98, 255));
                    canvas.drawRoundRect(new RectF(left, top, right, bottom), 14f, 14f, paint);

                    // Train windshield
                    paint.setColor(Color.rgb(187, 222, 251));
                    canvas.drawRoundRect(new RectF(left + 10, top + 12, right - 10, top + 42), 6f, 6f, paint);

                    // Headlights
                    paint.setColor(Color.rgb(255, 238, 88));
                    canvas.drawCircle(left + 22, bottom - 22, 12f, paint);
                    canvas.drawCircle(right - 22, bottom - 22, 12f, paint);

                    paint.setColor(Color.WHITE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(Math.max(16f, laneWidth * 0.18f));
                    paint.setFakeBoldText(true);
                    canvas.drawText("🚆 TRAIN", cx, top + obs.height * 0.55f, paint);
                    paint.setFakeBoldText(false);
                }
            }

            // 8. Draw Player
            float playerBaseY = height * 0.78f;
            float playerSize = laneWidth * 0.55f;

            float jumpOffset = 0f;
            if (isJumping) {
                float progress = jumpTime / jumpDuration;
                jumpOffset = (float) Math.sin(progress * Math.PI) * (playerSize * 1.6f);
            }

            float currentY = playerBaseY - jumpOffset;

            // Player Shadow (always on the ground)
            paint.setColor(Color.argb(90, 0, 0, 0));
            float shadowScale = isJumping ? Math.max(0.4f, 1f - (jumpOffset / (playerSize * 2f))) : 1f;
            canvas.drawOval(new RectF(
                    playerCurrentX - playerSize * 0.4f * shadowScale,
                    playerBaseY + playerSize * 0.35f,
                    playerCurrentX + playerSize * 0.4f * shadowScale,
                    playerBaseY + playerSize * 0.55f
            ), paint);

            // Draw Runner Character
            if (isSliding) {
                // Sliding / crouching runner
                paint.setColor(Color.rgb(255, 87, 34)); // Orange jacket
                canvas.drawRoundRect(new RectF(
                        playerCurrentX - playerSize * 0.45f,
                        currentY - playerSize * 0.2f,
                        playerCurrentX + playerSize * 0.45f,
                        currentY + playerSize * 0.35f
                ), 12f, 12f, paint);

                // Slide Dust Effect
                paint.setColor(Color.argb(120, 255, 255, 255));
                canvas.drawCircle(playerCurrentX - playerSize * 0.4f, currentY + playerSize * 0.3f, 8f, paint);
                canvas.drawCircle(playerCurrentX + playerSize * 0.4f, currentY + playerSize * 0.3f, 8f, paint);

                // Head
                paint.setColor(Color.rgb(255, 204, 128));
                canvas.drawCircle(playerCurrentX, currentY - playerSize * 0.22f, playerSize * 0.22f, paint);

            } else {
                // Standing / Running / Jumping Runner
                // Body
                paint.setColor(Color.rgb(230, 74, 25)); // Cool Runner Hoodie
                canvas.drawRoundRect(new RectF(
                        playerCurrentX - playerSize * 0.35f,
                        currentY - playerSize * 0.25f,
                        playerCurrentX + playerSize * 0.35f,
                        currentY + playerSize * 0.35f
                ), 14f, 14f, paint);

                // Head
                paint.setColor(Color.rgb(255, 204, 128)); // Face
                canvas.drawCircle(playerCurrentX, currentY - playerSize * 0.48f, playerSize * 0.24f, paint);

                // Cap
                paint.setColor(Color.rgb(25, 118, 210)); // Blue Cap
                canvas.drawArc(new RectF(
                        playerCurrentX - playerSize * 0.26f,
                        currentY - playerSize * 0.72f,
                        playerCurrentX + playerSize * 0.26f,
                        currentY - playerSize * 0.35f
                ), 180, 180, true, paint);

                // Eyes / Visor
                paint.setColor(Color.rgb(33, 33, 33));
                canvas.drawCircle(playerCurrentX - 6f, currentY - playerSize * 0.48f, 3.5f, paint);
                canvas.drawCircle(playerCurrentX + 6f, currentY - playerSize * 0.48f, 3.5f, paint);

                // Legs / Sneakers
                paint.setColor(Color.rgb(38, 50, 56));
                canvas.drawRoundRect(new RectF(
                        playerCurrentX - playerSize * 0.28f,
                        currentY + playerSize * 0.32f,
                        playerCurrentX - playerSize * 0.08f,
                        currentY + playerSize * 0.52f
                ), 6f, 6f, paint);
                canvas.drawRoundRect(new RectF(
                        playerCurrentX + playerSize * 0.08f,
                        currentY + playerSize * 0.32f,
                        playerCurrentX + playerSize * 0.28f,
                        currentY + playerSize * 0.52f
                ), 6f, 6f, paint);
            }

            // 9. Game Over Canvas Overlay
            if (gameOver) {
                paint.setColor(Color.argb(190, 0, 0, 0));
                canvas.drawRect(0, 0, width, height, paint);

                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(Math.max(40f, width * 0.085f));
                paint.setFakeBoldText(true);
                canvas.drawText("💥 GAME OVER", width / 2f, height / 2f - 40, paint);

                paint.setColor(Color.rgb(255, 215, 0));
                paint.setTextSize(Math.max(24f, width * 0.052f));
                canvas.drawText("Score: " + calculateScore(), width / 2f, height / 2f + 15, paint);

                paint.setColor(Color.rgb(176, 190, 197));
                paint.setTextSize(Math.max(18f, width * 0.038f));
                paint.setFakeBoldText(false);
                canvas.drawText("Distance: " + (int) distance + "m  •  Coins: " + coins, width / 2f, height / 2f + 55, paint);
            }
        }

        // SWIPE & TOUCH CONTROLS
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (gameOver) {
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownX = event.getX();
                    touchDownY = event.getY();
                    return true;

                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - touchDownX;
                    float dy = event.getY() - touchDownY;
                    float minSwipe = 35f;

                    if (Math.abs(dx) > minSwipe || Math.abs(dy) > minSwipe) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            if (dx > 0) {
                                moveRight();
                            } else {
                                moveLeft();
                            }
                        } else {
                            if (dy > 0) {
                                slide();
                            } else {
                                jump();
                            }
                        }
                    } else {
                        // Tap detection: tap left half -> move left, tap right half -> move right
                        if (event.getX() < getWidth() * 0.45f) {
                            moveLeft();
                        } else if (event.getX() > getWidth() * 0.55f) {
                            moveRight();
                        } else {
                            jump();
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

        // Inner data classes
        private static class Obstacle {
            int lane;
            int type;
            float y;
            float height;
        }

        private static class Coin {
            int lane;
            float y;
        }
    }
}
