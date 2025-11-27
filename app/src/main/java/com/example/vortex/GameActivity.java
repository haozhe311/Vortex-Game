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

public class GameActivity extends AppCompatActivity {

    private TextView tvLevel, tvTime, tvScore;
    private GridLayout gameGrid;

    private int currentLevel;
    private int accumulatedScore = 0; // Score from previous levels
    private int currentLevelScore = 0; // Score earned in THIS level

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

        // Get data passed from previous activity
        currentLevel = getIntent().getIntExtra("SELECTED_LEVEL", 1);
        accumulatedScore = getIntent().getIntExtra("ACCUMULATED_SCORE", 0);

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

    private void startLevel() {
        tvLevel.setText("LVL: " + currentLevel);
        currentLevelScore = 0;
        updateScoreDisplay();

        cellsPerSide = currentLevel + 1;
        generateGrid(cellsPerSide);
        highlightRandomCell();

        if (timer != null) timer.cancel();

        timer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
                tvTime.setText(String.format("T: %.1f", millisUntilFinished / 1000.0));
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
        // Show Total Score (Previous + Current)
        int total = accumulatedScore + currentLevelScore;
        tvScore.setText("PTS: " + total);
    }

    private void generateGrid(int side) {
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(side);
        gameGrid.setRowCount(side);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
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

    private void highlightRandomCell() {
        if (correctViewId != -1) {
            View prev = findViewById(correctViewId);
            if (prev != null) {
                prev.getBackground().clearColorFilter();
                prev.setRotation(0);
            }
        }
        int totalCells = cellsPerSide * cellsPerSide;
        Random random = new Random();
        correctViewId = random.nextInt(totalCells);

        View target = findViewById(correctViewId);
        if (target != null) {
            target.getBackground().setColorFilter(COLOR_CYAN, PorterDuff.Mode.SRC_ATOP);
            target.setScaleX(0.0f);
            target.setScaleY(0.0f);
            target.animate().scaleX(1.0f).scaleY(1.0f)
                    .setInterpolator(new OvershootInterpolator()).setDuration(300).start();
        }
    }

    private void checkHit(View v) {
        if (v.getId() == correctViewId) {
            currentLevelScore++;
            updateScoreDisplay();

            if(soundLoaded) soundPool.play(soundHit, 1, 1, 0, 0, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { vibrator.vibrate(50); }

            v.animate().rotation(360).setDuration(200).start();
            highlightRandomCell();
        } else {
            if(soundLoaded) soundPool.play(soundMiss, 1, 1, 0, 0, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { vibrator.vibrate(200); }

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

    private void handleGameEnd() {
        // Unlock Logic
        if (currentLevel < 4) {
            SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
            int currentMax = prefs.getInt("unlocked_level", 1);
            if (currentLevel + 1 > currentMax) {
                prefs.edit().putInt("unlocked_level", currentLevel + 1).apply();
            }
        }

        // Just pass data to GameOverActivity
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