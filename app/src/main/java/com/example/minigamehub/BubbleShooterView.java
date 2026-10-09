package com.example.minigamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class BubbleShooterView extends View {

    public interface GameListener {
        void onScoreChanged(int score);
        void onLevelChanged(int level);
        void onGameOver(int score);
    }

    private static final int COLS_EVEN = 8;
    private static final int COLS_ODD = 7;
    private static final int MAX_ROWS = 13;
    private static final int DANGER_ROW = 10;
    private static final int MAX_MISSES = 5;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private GameListener gameListener;

    // Grid: -1 means empty, 0..4 means color index
    private final int[][] grid = new int[MAX_ROWS][COLS_EVEN];

    private final int[] bubbleColors = {
            Color.rgb(239, 83, 80),   // Red
            Color.rgb(66, 133, 244),  // Blue
            Color.rgb(255, 202, 40),  // Yellow / Gold
            Color.rgb(76, 175, 80),   // Green
            Color.rgb(171, 71, 188)   // Purple
    };

    private int score = 0;
    private int level = 1;
    private int misses = 0;

    private boolean running = false;
    private boolean gameOver = false;

    // Dimensions
    private float bubbleRadius = 30f;
    private float rowHeight = 52f;
    private float cannonX = 0f;
    private float cannonY = 0f;

    // Aiming & Shooting
    private float aimAngle = (float) (-Math.PI / 2.0); // straight up
    private boolean isAiming = false;
    private boolean isFiring = false;

    private float flyingX = 0f;
    private float flyingY = 0f;
    private float flyingVx = 0f;
    private float flyingVy = 0f;
    private int flyingColor = 0;

    private int currentBubbleColor = 0;
    private int nextBubbleColor = 0;

    private long lastTime = 0;

    private final Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (!running || gameOver) {
                return;
            }

            long now = System.currentTimeMillis();
            float dt = (now - lastTime) / 1000f;
            lastTime = now;
            if (dt > 0.05f) {
                dt = 0.05f;
            }

            updatePhysics(dt);
            invalidate();

            if (!gameOver) {
                handler.postDelayed(this, 16); // ~60 FPS
            }
        }
    };

    public BubbleShooterView(Context context) {
        super(context);
        init();
    }

    public BubbleShooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleShooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        setBackgroundColor(Color.rgb(25, 20, 45));
        restartGame();
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
        if (gameListener != null) {
            gameListener.onScoreChanged(score);
            gameListener.onLevelChanged(level);
        }
    }

    public void startGame() {
        if (gameOver) {
            return;
        }
        if (!running) {
            running = true;
            lastTime = System.currentTimeMillis();
            handler.removeCallbacks(gameLoop);
            handler.post(gameLoop);
        }
    }

    public void stopGame() {
        running = false;
        handler.removeCallbacks(gameLoop);
    }

    public void restartGame() {
        handler.removeCallbacks(gameLoop);

        score = 0;
        level = 1;
        misses = 0;
        gameOver = false;
        running = true;
        isFiring = false;
        isAiming = false;
        aimAngle = (float) (-Math.PI / 2.0);

        initBoard();

        currentBubbleColor = pickRandomAvailableColor();
        nextBubbleColor = pickRandomAvailableColor();

        lastTime = System.currentTimeMillis();

        if (gameListener != null) {
            gameListener.onScoreChanged(score);
            gameListener.onLevelChanged(level);
        }

        handler.postDelayed(gameLoop, 150);
        invalidate();
    }

    private void initBoard() {
        for (int r = 0; r < MAX_ROWS; r++) {
            for (int c = 0; c < COLS_EVEN; c++) {
                grid[r][c] = -1;
            }
        }

        // Fill top 4 rows
        int initialRows = 4;
        for (int r = 0; r < initialRows; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                grid[r][c] = random.nextInt(bubbleColors.length);
            }
        }
    }

    private int pickRandomAvailableColor() {
        ArrayList<Integer> existing = new ArrayList<>();
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != -1 && !existing.contains(grid[r][c])) {
                    existing.add(grid[r][c]);
                }
            }
        }
        if (existing.isEmpty()) {
            return random.nextInt(bubbleColors.length);
        }
        return existing.get(random.nextInt(existing.size()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        bubbleRadius = width / (float) (COLS_EVEN * 2);
        rowHeight = bubbleRadius * (float) Math.sqrt(3);

        cannonX = width / 2.0f;
        // Position cannon comfortably above the bottom edge (leaving ~45dp touch space)
        cannonY = height - bubbleRadius * 2.2f - 18f;
    }

    private void updatePhysics(float dt) {
        if (!isFiring) {
            return;
        }

        float width = getWidth();

        // Move flying bubble
        flyingX += flyingVx * dt;
        flyingY += flyingVy * dt;

        // Wall bounce
        if (flyingX - bubbleRadius <= 0) {
            flyingX = bubbleRadius;
            flyingVx = -flyingVx;
        } else if (flyingX + bubbleRadius >= width) {
            flyingX = width - bubbleRadius;
            flyingVx = -flyingVx;
        }

        // Ceiling collision
        if (flyingY - bubbleRadius <= 0) {
            attachFlyingBubble();
            return;
        }

        // Collision with existing bubbles
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != -1) {
                    float bx = getBubbleCenterX(r, c);
                    float by = getBubbleCenterY(r);
                    float dx = flyingX - bx;
                    float dy = flyingY - by;
                    float distSq = dx * dx + dy * dy;
                    float threshold = (bubbleRadius * 2f) * 0.94f;
                    if (distSq <= threshold * threshold) {
                        attachFlyingBubble();
                        return;
                    }
                }
            }
        }
    }

    private float getBubbleCenterX(int r, int c) {
        if (r % 2 == 0) {
            return bubbleRadius + c * (bubbleRadius * 2f);
        } else {
            return (bubbleRadius * 2f) + c * (bubbleRadius * 2f);
        }
    }

    private float getBubbleCenterY(int r) {
        return bubbleRadius + r * rowHeight;
    }

    private void attachFlyingBubble() {
        isFiring = false;

        // Find nearest unoccupied grid cell
        int bestR = 0;
        int bestC = 0;
        float minDistance = Float.MAX_VALUE;

        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == -1) {
                    // In row 0, or adjacent to an existing bubble
                    if (r == 0 || hasOccupiedNeighbor(r, c)) {
                        float bx = getBubbleCenterX(r, c);
                        float by = getBubbleCenterY(r);
                        float dx = flyingX - bx;
                        float dy = flyingY - by;
                        float distSq = dx * dx + dy * dy;
                        if (distSq < minDistance) {
                            minDistance = distSq;
                            bestR = r;
                            bestC = c;
                        }
                    }
                }
            }
        }

        // Snap bubble
        grid[bestR][bestC] = flyingColor;

        // Check for Match-3 or more connected bubbles of same color
        ArrayList<Point> matched = findMatches(bestR, bestC, flyingColor);

        if (matched.size() >= 3) {
            // Remove matched bubbles
            for (Point p : matched) {
                grid[p.r][p.c] = -1;
            }

            int pointsGained = matched.size() * 10;

            // Remove floating / detached bubbles
            int dropped = removeFloatingBubbles();
            pointsGained += dropped * 15;

            score += pointsGained;
            updateLevel();

            if (gameListener != null) {
                gameListener.onScoreChanged(score);
            }

            // Check if entire board is cleared!
            if (isBoardCleared()) {
                score += 150;
                level++;
                initBoard();
                if (gameListener != null) {
                    gameListener.onScoreChanged(score);
                    gameListener.onLevelChanged(level);
                }
            }

        } else {
            // Missed shot
            misses++;
            if (misses >= MAX_MISSES) {
                misses = 0;
                dropGridDown();
            }
        }

        // Check for Game Over
        checkGameOver();

        // Prepare next shot
        currentBubbleColor = nextBubbleColor;
        nextBubbleColor = pickRandomAvailableColor();
    }

    private boolean hasOccupiedNeighbor(int r, int c) {
        for (Point n : getNeighbors(r, c)) {
            if (grid[n.r][n.c] != -1) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Point> getNeighbors(int r, int c) {
        ArrayList<Point> list = new ArrayList<>();
        boolean isEven = (r % 2 == 0);

        int[][] offsets = isEven ?
                new int[][]{{0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, -1}, {1, 0}} :
                new int[][]{{0, -1}, {0, 1}, {-1, 0}, {-1, 1}, {1, 0}, {1, 1}};

        for (int[] off : offsets) {
            int nr = r + off[0];
            int nc = c + off[1];
            if (nr >= 0 && nr < MAX_ROWS) {
                int maxCols = (nr % 2 == 0) ? COLS_EVEN : COLS_ODD;
                if (nc >= 0 && nc < maxCols) {
                    list.add(new Point(nr, nc));
                }
            }
        }
        return list;
    }

    private ArrayList<Point> findMatches(int startR, int startC, int targetColor) {
        ArrayList<Point> match = new ArrayList<>();
        boolean[][] visited = new boolean[MAX_ROWS][COLS_EVEN];

        Queue<Point> queue = new ArrayDeque<>();
        queue.add(new Point(startR, startC));
        visited[startR][startC] = true;

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            match.add(p);

            for (Point neighbor : getNeighbors(p.r, p.c)) {
                if (!visited[neighbor.r][neighbor.c] && grid[neighbor.r][neighbor.c] == targetColor) {
                    visited[neighbor.r][neighbor.c] = true;
                    queue.add(neighbor);
                }
            }
        }

        return match;
    }

    private int removeFloatingBubbles() {
        boolean[][] anchored = new boolean[MAX_ROWS][COLS_EVEN];
        Queue<Point> queue = new ArrayDeque<>();

        // Start BFS from all occupied cells in row 0
        for (int c = 0; c < COLS_EVEN; c++) {
            if (grid[0][c] != -1) {
                anchored[0][c] = true;
                queue.add(new Point(0, c));
            }
        }

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (Point n : getNeighbors(p.r, p.c)) {
                if (!anchored[n.r][n.c] && grid[n.r][n.c] != -1) {
                    anchored[n.r][n.c] = true;
                    queue.add(n);
                }
            }
        }

        // Remove any bubble that is not anchored
        int removedCount = 0;
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != -1 && !anchored[r][c]) {
                    grid[r][c] = -1;
                    removedCount++;
                }
            }
        }

        return removedCount;
    }

    private void dropGridDown() {
        // Shift rows down
        for (int r = MAX_ROWS - 1; r > 0; r--) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            int prevCols = ((r - 1) % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (c < prevCols) {
                    grid[r][c] = grid[r - 1][c];
                } else {
                    grid[r][c] = -1;
                }
            }
        }

        // Add new top row
        for (int c = 0; c < COLS_EVEN; c++) {
            grid[0][c] = random.nextInt(bubbleColors.length);
        }
    }

    private boolean isBoardCleared() {
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkGameOver() {
        for (int c = 0; c < COLS_EVEN; c++) {
            if (grid[DANGER_ROW][c] != -1) {
                endGame();
                return;
            }
        }
    }

    private void endGame() {
        if (gameOver) return;
        gameOver = true;
        running = false;
        handler.removeCallbacks(gameLoop);

        if (gameListener != null) {
            gameListener.onGameOver(score);
        }

        invalidate();
    }

    private void updateLevel() {
        int newLevel = 1 + (score / 120);
        if (newLevel != level) {
            level = newLevel;
            if (gameListener != null) {
                gameListener.onLevelChanged(level);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver || !running) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Only allow aiming upward
                if (y < cannonY - 15f) {
                    isAiming = true;
                    float angle = (float) Math.atan2(y - cannonY, x - cannonX);
                    // Restrict aiming angle between -165 deg and -15 deg
                    float minAngle = (float) (-Math.PI + 0.22);
                    float maxAngle = -0.22f;
                    if (angle < minAngle) angle = minAngle;
                    if (angle > maxAngle) angle = maxAngle;
                    aimAngle = angle;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isAiming && !isFiring && y < cannonY - 15f) {
                    shootBubble();
                }
                isAiming = false;
                invalidate();
                return true;
        }

        return true;
    }

    private void shootBubble() {
        if (isFiring || gameOver) return;

        isFiring = true;
        flyingX = cannonX;
        flyingY = cannonY;
        flyingColor = currentBubbleColor;

        float speed = Math.max(1600f, getWidth() * 3.5f);
        flyingVx = (float) (speed * Math.cos(aimAngle));
        flyingVy = (float) (speed * Math.sin(aimAngle));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        if (cannonX == 0f || cannonY == 0f) {
            calculateDimensions();
        }

        // 1. Background
        paint.setColor(Color.rgb(22, 18, 38));
        canvas.drawRect(0, 0, width, height, paint);

        // 2. Danger Line
        float dangerY = bubbleRadius + DANGER_ROW * rowHeight;
        paint.setColor(Color.rgb(239, 83, 80));
        paint.setStrokeWidth(3f);
        paint.setPathEffect(new DashPathEffect(new float[]{16, 12}, 0));
        canvas.drawLine(0, dangerY, width, dangerY, paint);
        paint.setPathEffect(null);

        paint.setColor(Color.argb(160, 239, 83, 80));
        paint.setTextSize(Math.max(16f, width * 0.032f));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("⚠️ DANGER LINE", width - 14f, dangerY - 6f, paint);

        // 3. Draw Grid Bubbles
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != -1) {
                    float bx = getBubbleCenterX(r, c);
                    float by = getBubbleCenterY(r);
                    drawSingleBubble(canvas, bx, by, bubbleRadius, bubbleColors[grid[r][c]]);
                }
            }
        }

        // 4. Draw Aiming Line (Trajectory)
        if (isAiming && !isFiring) {
            paint.setColor(Color.argb(140, 255, 255, 255));
            paint.setStrokeWidth(4f);
            paint.setPathEffect(new DashPathEffect(new float[]{14, 10}, 0));

            float guideLength = bubbleRadius * 8f;
            float endX = (float) (cannonX + guideLength * Math.cos(aimAngle));
            float endY = (float) (cannonY + guideLength * Math.sin(aimAngle));
            canvas.drawLine(cannonX, cannonY, endX, endY, paint);

            paint.setPathEffect(null);
        }

        // 5. Draw Flying Bubble
        if (isFiring) {
            drawSingleBubble(canvas, flyingX, flyingY, bubbleRadius, bubbleColors[flyingColor]);
        }

        // 6. Draw Cannon / Shooter Base
        // Cannon Turret Barrel
        paint.setColor(Color.rgb(74, 62, 107));
        paint.setStrokeWidth(bubbleRadius * 1.5f);
        float barrelLength = bubbleRadius * 1.9f;
        float bx = (float) (cannonX + barrelLength * Math.cos(aimAngle));
        float by = (float) (cannonY + barrelLength * Math.sin(aimAngle));
        canvas.drawLine(cannonX, cannonY, bx, by, paint);

        // Cannon Outer Disc
        paint.setColor(Color.rgb(46, 38, 70));
        canvas.drawCircle(cannonX, cannonY, bubbleRadius * 1.55f, paint);

        paint.setColor(Color.rgb(94, 78, 138));
        canvas.drawCircle(cannonX, cannonY, bubbleRadius * 1.25f, paint);

        // Current loaded bubble in cannon center
        if (!isFiring) {
            drawSingleBubble(canvas, cannonX, cannonY, bubbleRadius, bubbleColors[currentBubbleColor]);
        }

        // 7. Draw Next Bubble Indicator (placed to the left of the cannon)
        float nextX = cannonX - bubbleRadius * 3.4f;
        float nextY = cannonY;

        paint.setColor(Color.rgb(188, 169, 232));
        paint.setTextSize(Math.max(14f, width * 0.030f));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("NEXT", nextX, nextY - bubbleRadius * 1.3f, paint);

        drawSingleBubble(canvas, nextX, nextY, bubbleRadius * 0.85f, bubbleColors[nextBubbleColor]);

        // 8. Draw Miss Counter (placed to the right of the cannon)
        float missX = cannonX + bubbleRadius * 2.8f;
        float missY = cannonY;

        paint.setColor(Color.rgb(188, 169, 232));
        paint.setTextSize(Math.max(14f, width * 0.030f));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("FAILS: " + misses + "/" + MAX_MISSES, missX + 24f, missY - bubbleRadius * 1.3f, paint);

        for (int i = 0; i < MAX_MISSES; i++) {
            float dotX = missX + (i * 18f);
            float dotY = missY;
            if (i < misses) {
                paint.setColor(Color.rgb(239, 83, 80)); // Red miss dot
            } else {
                paint.setColor(Color.rgb(66, 54, 94));  // Empty dot
            }
            canvas.drawCircle(dotX, dotY, 6f, paint);
        }

        // 9. Game Over Overlay
        if (gameOver) {
            paint.setColor(Color.argb(205, 12, 10, 22));
            canvas.drawRect(0, 0, width, height, paint);

            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(Math.max(38f, width * 0.082f));
            paint.setFakeBoldText(true);
            canvas.drawText("💥 GAME OVER", width / 2f, height / 2f - 35, paint);

            paint.setColor(Color.rgb(255, 213, 79));
            paint.setTextSize(Math.max(24f, width * 0.052f));
            canvas.drawText("Final Score: " + score, width / 2f, height / 2f + 15, paint);

            paint.setColor(Color.rgb(188, 169, 232));
            paint.setTextSize(Math.max(18f, width * 0.038f));
            paint.setFakeBoldText(false);
            canvas.drawText("Reached Level " + level, width / 2f, height / 2f + 55, paint);
        }
    }

    private void drawSingleBubble(Canvas canvas, float cx, float cy, float r, int color) {
        // Main bubble body
        paint.setColor(color);
        canvas.drawCircle(cx, cy, r, paint);

        // Highlight shine
        paint.setColor(Color.argb(130, 255, 255, 255));
        canvas.drawCircle(cx - r * 0.32f, cy - r * 0.32f, r * 0.28f, paint);

        // Bubble border
        paint.setColor(Color.argb(60, 0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(gameLoop);
    }

    private static class Point {
        int r;
        int c;
        Point(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return r == point.r && c == point.c;
        }

        @Override
        public int hashCode() {
            return 31 * r + c;
        }
    }
}
