package com.example.vortex;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Random;

/**
 * Handles the core gameplay mechanics.
 * Responsibilities include:
 * 1. Generating the grid dynamically based on the current level.
 * 2. Managing the 5-second countdown timer.
 * 3. Handling user input (touch events) and determining hits vs. misses.
 * 4. Playing audio and haptic feedback.
 */
public class GameActivity extends AppCompatActivity {

    private TextView tvLevel, tvTime, tvScore;
    private GridLayout gameGrid;

    private int currentLevel;
    private int accumulatedScore = 0;
    private int currentLevelScore = 0;

    private CountDownTimer timer;
    private int correctViewId = -1;
    private int cellsPerSide;

    private SoundPool soundPool;
    private int soundHit, soundMiss;
    private boolean soundLoaded = false;
    private Vibrator vibrator;

    private int COLOR_CYAN, COLOR_PINK, COLOR_TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Retrieve level and score data passed from the previous activity.
        currentLevel = getIntent().getIntExtra("SELECTED_LEVEL", 1);
        accumulatedScore = getIntent().getIntExtra("ACCUMULATED_SCORE", 0);

        // Pre-fetch colors to avoid repeated resource lookups during gameplay.
        COLOR_CYAN = ContextCompat.getColor(this, R.color.cyber_cyan);
        COLOR_PINK = ContextCompat.getColor(this, R.color.cyber_pink);
        COLOR_TEXT = ContextCompat.getColor(this, R.color.cyber_text);

        tvLevel = findViewById(R.id.tvLevel);
        tvTime = findViewById(R.id.tvTime);
        tvScore = findViewById(R.id.tvScore);
        gameGrid = findViewById(R.id.gameGrid);

        initVibrator();
        initSoundPool();
        startLevel();
    }

    /**
     * Initializes the SoundPool for low-latency audio playback.
     */
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        try {
            soundHit = soundPool.load(this, R.raw.hit, 1);
            soundMiss = soundPool.load(this, R.raw.miss, 1);
            soundLoaded = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initVibrator() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Configures the level environment.
     * Sets up the grid based on level difficulty and starts the 5-second timer.
     */
    private void startLevel() {
        tvLevel.setText("LVL: " + currentLevel);
        currentLevelScore = 0;
        updateScoreDisplay();

        // Level 1 = 2x2, Level 2 = 3x3, etc.
        cellsPerSide = currentLevel + 1;
        generateGrid(cellsPerSide);
        highlightRandomCell();

        if (timer != null) timer.cancel();

        // 5000ms (5 seconds) timer with 100ms update interval.
        timer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
                tvTime.setText(String.format("T: %.1f", millisUntilFinished / 1000.0));

                // Visual warning: Change text color to PINK when time is running out.
                if (millisUntilFinished < 2000) {
                    tvTime.setTextColor(COLOR_PINK);
                } else {
                    tvTime.setTextColor(COLOR_TEXT);
                }
            }

            public void onFinish() {
                tvTime.setText("T: 0.0");
                handleGameEnd();
            }
        }.start();
    }

    private void updateScoreDisplay() {
        int total = accumulatedScore + currentLevelScore;
        tvScore.setText("PTS: " + total);
    }

    /**
     * Dynamically populates the GridLayout with Views.
     * Calculates cell size to fit within the screen width while maintaining a square aspect ratio.
     *
     * @param side The number of cells per row/column.
     */
    private void generateGrid(int side) {
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(side);
        gameGrid.setRowCount(side);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;

        // Calculate dynamic margins and padding to ensure cells fit on screen.
        int marginInPixels = (int) (4 * density);
        int parentPadding = (int) (40 * density);
        int totalMarginSpace = (marginInPixels * 2) * side;
        int cellSize = (screenWidth - parentPadding - totalMarginSpace) / side;

        for (int i = 0; i < side * side; i++) {
            View cell = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellSize;
            params.height = cellSize;
            params.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
            cell.setLayoutParams(params);
            cell.setBackgroundResource(R.drawable.cell_cyber);
            cell.setId(i);
            cell.setOnClickListener(this::checkHit);
            gameGrid.addView(cell);
        }
    }

    /**
     * Logic for selecting a random View to highlight.
     * Resets the previous target and applies visual effects to the new target.
     */
    private void highlightRandomCell() {
        // Reset previous cell
        if (correctViewId != -1) {
            View prev = findViewById(correctViewId);
            if (prev != null) {
                prev.getBackground().clearColorFilter();
                prev.setRotation(0);
            }
        }

        // Select new random cell
        int totalCells = cellsPerSide * cellsPerSide;
        Random random = new Random();
        correctViewId = random.nextInt(totalCells);

        View target = findViewById(correctViewId);
        if (target != null) {
            target.getBackground().setColorFilter(COLOR_CYAN, PorterDuff.Mode.SRC_ATOP);

            // Pop-in animation for visual feedback
            target.setScaleX(0.0f);
            target.setScaleY(0.0f);
            target.animate().scaleX(1.0f).scaleY(1.0f)
                    .setInterpolator(new OvershootInterpolator()).setDuration(300).start();
        }
    }

    /**
     * Processes user taps on the grid.
     *
     * @param v The View that was clicked.
     */
    private void checkHit(View v) {
        if (v.getId() == correctViewId) {
            // SUCCESS
            currentLevelScore++;
            updateScoreDisplay();

            if(soundLoaded) soundPool.play(soundHit, 1, 1, 0, 0, 1);

            // Short, sharp vibration for success
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { vibrator.vibrate(50); }

            v.animate().rotation(360).setDuration(200).start();
            highlightRandomCell();
        } else {
            // FAILURE
            if(soundLoaded) soundPool.play(soundMiss, 1, 1, 0, 0, 1);

            // Longer vibration for error
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { vibrator.vibrate(200); }

            // Visual Shake Animation
            Drawable bg = v.getBackground();
            bg.setColorFilter(COLOR_PINK, PorterDuff.Mode.SRC_ATOP);
            ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, 20, -20, 20, -20, 0);
            shake.setDuration(400);
            shake.setInterpolator(new CycleInterpolator(3));
            shake.start();
            shake.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    bg.clearColorFilter();
                }
            });
        }
    }

    /**
     * Called when the timer runs out.
     * Transitions to the Game Over screen and unlocks levels if applicable.
     */
    private void handleGameEnd() {
        if (currentLevel < 4) {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            int currentMax = prefs.getInt("unlocked_level", 1);
            if (currentLevel + 1 > currentMax) {
                prefs.edit().putInt("unlocked_level", currentLevel + 1).apply();
            }
        }

        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
        intent.putExtra("FINISHED_LEVEL", currentLevel);
        intent.putExtra("LEVEL_SCORE", currentLevelScore);
        intent.putExtra("TOTAL_SCORE", accumulatedScore + currentLevelScore);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (soundPool != null) soundPool.release();
    }
}