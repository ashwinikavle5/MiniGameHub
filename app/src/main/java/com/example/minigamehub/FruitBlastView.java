package com.example.minigamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class FruitBlastView extends View {

    public interface GameListener {
        void onScoreChanged(int score);
        void onLevelChanged(int level);
        void onGameOver(int score);
        void onLevelCompleted(int moves, int level);
    }

    private static final int TUBE_CAPACITY = 4;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private GameListener gameListener;

    // Tube colors palette
    private final int[] ballColors = {
            Color.rgb(229, 57, 53),   // 0: Crimson Red
            Color.rgb(30, 136, 229),  // 1: Royal Blue
            Color.rgb(67, 160, 71),   // 2: Emerald Green
            Color.rgb(253, 216, 53),  // 3: Amber Yellow
            Color.rgb(142, 36, 170),  // 4: Orchid Purple
            Color.rgb(251, 140, 0)    // 5: Vivid Orange
    };

    // Tubes: each tube is an ArrayList of color indices (0 to TUBE_CAPACITY-1)
    private final ArrayList<ArrayList<Integer>> tubes = new ArrayList<>();
    private final ArrayList<RectF> tubeBounds = new ArrayList<>();

    private int level = 1;
    private int moves = 0;
    private int selectedTube = -1;
    private boolean isLevelComplete = false;

    public FruitBlastView(Context context) {
        super(context);
        init();
    }

    public FruitBlastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FruitBlastView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
        setBackgroundColor(Color.rgb(24, 30, 46));
        setupLevel(1);
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
        if (gameListener != null) {
            gameListener.onScoreChanged(moves);
            gameListener.onLevelChanged(level);
        }
    }

    public void restartGame() {
        setupLevel(level);
    }

    public void nextLevel() {
        setupLevel(level + 1);
    }

    public void startGame() {
        // Active view
        invalidate();
    }

    public void stopGame() {
        // Paused view
    }

    private void setupLevel(int newLevel) {
        level = newLevel;
        moves = 0;
        selectedTube = -1;
        isLevelComplete = false;

        tubes.clear();

        // Determine number of colors and empty tubes
        int numColors;
        int numEmptyTubes;

        if (level == 1) {
            numColors = 2;
            numEmptyTubes = 1; // 3 tubes
        } else if (level == 2) {
            numColors = 3;
            numEmptyTubes = 1; // 4 tubes
        } else if (level == 3) {
            numColors = 3;
            numEmptyTubes = 2; // 5 tubes
        } else if (level == 4) {
            numColors = 4;
            numEmptyTubes = 2; // 6 tubes
        } else {
            numColors = Math.min(5, 4 + (level - 4));
            numEmptyTubes = 2; // 7 tubes
        }

        int totalTubes = numColors + numEmptyTubes;

        // 1. Fill filled tubes in solved state
        for (int i = 0; i < numColors; i++) {
            ArrayList<Integer> tube = new ArrayList<>();
            for (int k = 0; k < TUBE_CAPACITY; k++) {
                tube.add(i);
            }
            tubes.add(tube);
        }

        // 2. Add empty tubes
        for (int i = 0; i < numEmptyTubes; i++) {
            tubes.add(new ArrayList<>());
        }

        // 3. Shuffle using valid reverse moves to GUARANTEE 100% solvability
        shuffleTubes(totalTubes, 35 + level * 5);

        if (gameListener != null) {
            gameListener.onScoreChanged(moves);
            gameListener.onLevelChanged(level);
        }

        invalidate();
    }

    private void shuffleTubes(int totalTubes, int steps) {
        int successfulMoves = 0;
        int maxAttempts = steps * 6;
        int attempts = 0;

        while (successfulMoves < steps && attempts < maxAttempts) {
            attempts++;
            int from = random.nextInt(totalTubes);
            int to = random.nextInt(totalTubes);

            if (from == to) continue;

            ArrayList<Integer> fromTube = tubes.get(from);
            ArrayList<Integer> toTube = tubes.get(to);

            if (fromTube.isEmpty() || toTube.size() >= TUBE_CAPACITY) continue;

            // In reverse shuffle, any move between tubes is allowed as long as destination isn't full
            int ball = fromTube.remove(fromTube.size() - 1);
            toTube.add(ball);
            successfulMoves++;
        }

        // Check if accidentally already solved; if so, perform a few more moves
        if (checkSolved()) {
            for (int i = 0; i < totalTubes; i++) {
                if (!tubes.get(i).isEmpty()) {
                    for (int j = 0; j < totalTubes; j++) {
                        if (i != j && tubes.get(j).size() < TUBE_CAPACITY) {
                            tubes.get(j).add(tubes.get(i).remove(tubes.get(i).size() - 1));
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private boolean checkSolved() {
        for (ArrayList<Integer> tube : tubes) {
            if (tube.isEmpty()) continue;
            if (tube.size() != TUBE_CAPACITY) return false;
            int firstColor = tube.get(0);
            for (int c : tube) {
                if (c != firstColor) return false;
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLevelComplete) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            int clickedTube = -1;
            for (int i = 0; i < tubeBounds.size(); i++) {
                if (tubeBounds.get(i).contains(x, y)) {
                    clickedTube = i;
                    break;
                }
            }

            if (clickedTube != -1) {
                handleTubeClick(clickedTube);
            }
            return true;
        }

        return true;
    }

    private void handleTubeClick(int tubeIndex) {
        if (selectedTube == -1) {
            // Pick ball from tube if not empty
            if (!tubes.get(tubeIndex).isEmpty()) {
                selectedTube = tubeIndex;
                invalidate();
            }
        } else if (selectedTube == tubeIndex) {
            // Deselect
            selectedTube = -1;
            invalidate();
        } else {
            // Attempt to move ball from selectedTube to tubeIndex
            ArrayList<Integer> fromList = tubes.get(selectedTube);
            ArrayList<Integer> toList = tubes.get(tubeIndex);

            int ballToMove = fromList.get(fromList.size() - 1);

            boolean canMove = false;
            if (toList.size() < TUBE_CAPACITY) {
                if (toList.isEmpty()) {
                    canMove = true;
                } else {
                    int topDestinationColor = toList.get(toList.size() - 1);
                    if (topDestinationColor == ballToMove) {
                        canMove = true;
                    }
                }
            }

            if (canMove) {
                fromList.remove(fromList.size() - 1);
                toList.add(ballToMove);
                moves++;
                selectedTube = -1;

                if (gameListener != null) {
                    gameListener.onScoreChanged(moves);
                }

                if (checkSolved()) {
                    isLevelComplete = true;
                    int calculatedScore = Math.max(10, 100 - (moves * 2));
                    if (gameListener != null) {
                        gameListener.onGameOver(calculatedScore);
                        gameListener.onLevelCompleted(moves, level);
                    }
                }
            } else {
                // If clicked tube is not empty, switch selection to it
                if (!toList.isEmpty()) {
                    selectedTube = tubeIndex;
                } else {
                    selectedTube = -1;
                }
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0 || tubes.isEmpty()) return;

        int totalTubes = tubes.size();
        tubeBounds.clear();

        // Layout tubes in 1 or 2 rows
        int numRows = (totalTubes > 4) ? 2 : 1;
        int tubesInRow1 = (numRows == 1) ? totalTubes : ((totalTubes + 1) / 2);
        int tubesInRow2 = (numRows == 1) ? 0 : (totalTubes - tubesInRow1);

        float paddingH = width * 0.05f;
        float availableWidth = width - (paddingH * 2);

        float tubeWidth = Math.min(width * 0.18f, availableWidth / (float) (Math.max(tubesInRow1, 4)) * 0.72f);
        float tubeHeight = tubeWidth * 3.4f;
        float ballRadius = tubeWidth * 0.42f;

        float startY = (numRows == 1) ? (height * 0.50f - tubeHeight * 0.45f) : (height * 0.16f);
        float rowSpacing = tubeHeight + (height * 0.14f);

        for (int i = 0; i < totalTubes; i++) {
            int row = (numRows == 1) ? 0 : (i < tubesInRow1 ? 0 : 1);
            int col = (row == 0) ? i : (i - tubesInRow1);
            int countInThisRow = (row == 0) ? tubesInRow1 : tubesInRow2;

            float spacingX = availableWidth / (float) countInThisRow;
            float cx = paddingH + (col + 0.5f) * spacingX;
            float topY = startY + row * rowSpacing;

            RectF bounds = new RectF(cx - tubeWidth / 2f, topY - ballRadius * 1.6f, cx + tubeWidth / 2f, topY + tubeHeight);
            tubeBounds.add(bounds);

            drawTube(canvas, cx, topY, tubeWidth, tubeHeight, ballRadius, i);
        }

        // Draw Level Complete Message
        if (isLevelComplete) {
            paint.setColor(Color.argb(200, 18, 22, 34));
            canvas.drawRect(0, 0, width, height, paint);

            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(Math.max(34f, width * 0.080f));
            paint.setFakeBoldText(true);
            canvas.drawText("🎉 LEVEL COMPLETE!", width / 2f, height / 2f - 40, paint);

            paint.setColor(Color.rgb(255, 215, 0));
            paint.setTextSize(Math.max(22f, width * 0.050f));
            canvas.drawText("Completed in " + moves + " moves! ✨", width / 2f, height / 2f + 15, paint);

            paint.setColor(Color.rgb(176, 190, 197));
            paint.setTextSize(Math.max(16f, width * 0.038f));
            paint.setFakeBoldText(false);
            canvas.drawText("Tap 'NEXT LEVEL' below to continue", width / 2f, height / 2f + 55, paint);
        }
    }

    private void drawTube(Canvas canvas, float cx, float topY, float tubeWidth, float tubeHeight, float ballRadius, int tubeIndex) {
        float left = cx - tubeWidth / 2f;
        float right = cx + tubeWidth / 2f;
        float bottomY = topY + tubeHeight;

        // Tube Glass Background
        paint.setColor(Color.argb(45, 255, 255, 255));
        paint.setStyle(Paint.Style.FILL);
        RectF tubeRect = new RectF(left, topY, right, bottomY);
        canvas.drawRoundRect(tubeRect, tubeWidth * 0.45f, tubeWidth * 0.45f, paint);

        // Tube Glass Outline
        paint.setColor(selectedTube == tubeIndex ? Color.rgb(255, 215, 0) : Color.argb(180, 207, 216, 220));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selectedTube == tubeIndex ? 5f : 3.5f);
        canvas.drawRoundRect(tubeRect, tubeWidth * 0.45f, tubeWidth * 0.45f, paint);

        // Tube Rim (top collar)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selectedTube == tubeIndex ? Color.rgb(255, 215, 0) : Color.rgb(207, 216, 220));
        canvas.drawRoundRect(new RectF(left - 4f, topY - 3f, right + 4f, topY + 4f), 3f, 3f, paint);

        ArrayList<Integer> balls = tubes.get(tubeIndex);
        int ballCount = balls.size();

        float ballDiameter = ballRadius * 2f;
        float startBallY = bottomY - ballRadius - 4f;

        for (int k = 0; k < ballCount; k++) {
            int colorIndex = balls.get(k);
            int color = ballColors[colorIndex % ballColors.length];

            // If this is the top ball and this tube is currently selected, lift it above the tube!
            if (k == ballCount - 1 && selectedTube == tubeIndex) {
                float liftedY = topY - ballRadius * 1.3f;
                drawSingleBall(canvas, cx, liftedY, ballRadius, color, true);
            } else {
                float by = startBallY - (k * ballDiameter);
                drawSingleBall(canvas, cx, by, ballRadius, color, false);
            }
        }
    }

    private void drawSingleBall(Canvas canvas, float cx, float cy, float r, int color, boolean isSelected) {
        // Selection glow
        if (isSelected) {
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawCircle(cx, cy, r + 5f, paint);
        }

        // Ball body
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, r, paint);

        // Ball 3D shine
        paint.setColor(Color.argb(140, 255, 255, 255));
        canvas.drawCircle(cx - r * 0.32f, cy - r * 0.32f, r * 0.28f, paint);

        // Subtle border
        paint.setColor(Color.argb(50, 0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
