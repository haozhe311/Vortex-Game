package com.example.vortex;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

/**
 * Purpose: Handles the core gameplay loop.
 * Features: Dynamic grid generation, Countdown timer, Score tracking,
 * Audio/Haptic feedback, and Animations.
 */
public class GameActivity extends AppCompatActivity {

    // UI Components
    private TextView tvLevel, tvTime, tvScore;
    private GridLayout gameGrid;

    // Game State
    private int currentLevel = 1;
    private int score = 0;
    private CountDownTimer timer;
    private int correctViewId = -1;
    private int cellsPerSide = 2;

    // Media & Feedback
    private SoundPool soundPool;
    private int soundHit, soundMiss;
    private boolean soundLoaded = false;
    private Vibrator vibrator;

    // Theme Colors
    private int COLOR_CYAN;
    private int COLOR_PINK;
    private int COLOR_TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Load resources and colors
        COLOR_CYAN = ContextCompat.getColor(this, R.color.cyber_cyan);
        COLOR_PINK = ContextCompat.getColor(this, R.color.cyber_pink);
        COLOR_TEXT = ContextCompat.getColor(this, R.color.cyber_text);

        // Bind Views
        tvLevel = findViewById(R.id.tvLevel);
        tvTime = findViewById(R.id.tvTime);
        tvScore = findViewById(R.id.tvScore);
        gameGrid = findViewById(R.id.gameGrid);

        // Initialize Hardware Services
        initVibrator();
        initSoundPool();

        // Begin the first level
        startLevel();
    }

    /**
     * Initializes the SoundPool for efficient audio playback.
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the Vibrator service.
     */
    private void initVibrator() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Sets up the grid and timer for the current level.
     */
    private void startLevel() {
        tvLevel.setText("LVL: " + currentLevel);

        // Calculate grid size: Level 1 (2x2), Level 2 (3x3), etc.
        cellsPerSide = currentLevel + 1;

        generateGrid(cellsPerSide);
        highlightRandomCell();

        // Reset and start the 5-second timer
        if (timer != null) timer.cancel();
        startTimer();
    }

    /**
     * Starts a 5-second countdown timer.
     * Updates the UI every 100ms.
     */
    private void startTimer() {
        timer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
                tvTime.setText(String.format("T: %.1f", millisUntilFinished / 1000.0));

                // Change color to warn user when time is running low (< 2s)
                if (millisUntilFinished < 2000) {
                    tvTime.setTextColor(COLOR_PINK);
                } else {
                    tvTime.setTextColor(COLOR_TEXT);
                }
            }

            public void onFinish() {
                tvTime.setText("T: 0.0");
                handleLevelComplete();
            }
        }.start();
    }

    /**
     * Progresses to the next level or ends the game if Level 4 is finished.
     */
    private void handleLevelComplete() {
        if (currentLevel < 4) {
            currentLevel++;
            Toast.makeText(GameActivity.this, "LEVEL UP >> " + currentLevel, Toast.LENGTH_SHORT).show();
            startLevel();
        } else {
            endGame();
        }
    }

    /**
     * Dynamically creates views (cells) and adds them to the GridLayout.
     * Calculates cell size based on screen width to ensure responsiveness.
     * @param side The number of cells per row/column.
     */
    private void generateGrid(int side) {
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(side);
        gameGrid.setRowCount(side);

        // Calculate screen dimensions for dynamic sizing
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;

        int marginInPixels = (int) (4 * density);
        int parentPadding = (int) (40 * density);
        int totalMarginSpace = (marginInPixels * 2) * side;

        // Formula ensures cells fit perfectly within screen width
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
     * Selects a random cell to highlight as the target.
     * Applies a pop-up animation to the new target.
     */
    private void highlightRandomCell() {
        // Reset the previous target if it exists
        if (correctViewId != -1) {
            View prev = findViewById(correctViewId);
            if (prev != null) {
                prev.getBackground().clearColorFilter();
                prev.setRotation(0);
            }
        }

        // Select new random target
        int totalCells = cellsPerSide * cellsPerSide;
        Random random = new Random();
        correctViewId = random.nextInt(totalCells);

        // Highlight and animate the new target
        View target = findViewById(correctViewId);
        if (target != null) {
            target.getBackground().setColorFilter(COLOR_CYAN, PorterDuff.Mode.SRC_ATOP);

            // "Pop" animation using OvershootInterpolator
            target.setScaleX(0.0f);
            target.setScaleY(0.0f);
            target.animate()
                    .scaleX(1.0f).scaleY(1.0f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(300)
                    .start();
        }
    }

    /**
     * Validates user input when a cell is tapped.
     * @param v The view that was clicked.
     */
    private void checkHit(View v) {
        if (v.getId() == correctViewId) {
            // Correct Hit Logic
            score++;
            tvScore.setText("PTS: " + score);
            playSound(soundHit);
            vibrateDevice(50); // Short vibration

            // Spin animation for feedback
            v.animate().rotation(360).setDuration(200).start();

            highlightRandomCell();
        } else {
            // Wrong Hit Logic
            playSound(soundMiss);
            vibrateDevice(200); // Long vibration

            // Turn cell red temporarily
            Drawable bg = v.getBackground();
            bg.setColorFilter(COLOR_PINK, PorterDuff.Mode.SRC_ATOP);

            // Shake animation for error feedback
            ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, 20, -20, 20, -20, 0);
            shake.setDuration(400);
            shake.setInterpolator(new CycleInterpolator(3));
            shake.start();

            // Reset color after animation
            shake.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    bg.clearColorFilter();
                }
            });
        }
    }

    /**
     * Helper method to play sounds safely.
     */
    private void playSound(int soundId) {
        if (soundLoaded) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    /**
     * Helper method to trigger vibration compatible with different Android versions.
     */
    private void vibrateDevice(long milliseconds) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    /**
     * Handles Game Over logic.
     * Checks if score is in Top 25 and prompts for name if applicable.
     */
    private void endGame() {
        DBHelper db = new DBHelper(this);
        if (db.isTop25(score)) {
            showSaveDialog(db);
        } else {
            // Standard Game Over dialog
            new AlertDialog.Builder(this)
                    .setTitle("SYSTEM FAILURE")
                    .setMessage("Final Score: " + score)
                    .setPositiveButton("RETRY", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Shows a dialog for the user to enter their name for the high score table.
     */
    private void showSaveDialog(DBHelper db) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("NEW HIGH SCORE");
        builder.setMessage("Enter Your Name:");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("UPLOAD", (dialog, which) -> {
            String name = input.getText().toString();
            if (name.isEmpty()) name = "Guest";
            db.addScore(name, score);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources to prevent memory leaks
        if (timer != null) timer.cancel();
        if (soundPool != null) soundPool.release();
    }
}