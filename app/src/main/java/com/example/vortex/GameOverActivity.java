package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int level = getIntent().getIntExtra("FINISHED_LEVEL", 1);
        int score = getIntent().getIntExtra("FINAL_SCORE", 0);

        TextView tvTitle = findViewById(R.id.tvGameOverTitle);
        TextView tvSub = findViewById(R.id.tvGameOverSub);

        tvSub.setText("LEVEL " + level + " COMPLETE\nSCORE: " + score);

        // Initialize Buttons
        Button btnMainMenu = findViewById(R.id.btnMainMenu); // NEW BUTTON
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        Button btnRetry = findViewById(R.id.btnRetry);
        Button btnNext = findViewById(R.id.btnNextLevel);

        // 0. NEW: Main Menu Logic
        btnMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            // This flag clears the history so "Back" button won't return here
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 1. Leaderboard (Show only for this level)
        btnLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, ScoreActivity.class);
            intent.putExtra("LEVEL_FILTER", level);
            startActivity(intent);
        });

        // 2. Retry (Restart same level)
        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            intent.putExtra("SELECTED_LEVEL", level);
            startActivity(intent);
            finish();
        });

        // 3. Next Level logic
        if (level >= 4) {
            btnNext.setVisibility(View.GONE); // No next level after 4
        } else {
            btnNext.setOnClickListener(v -> {
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtra("SELECTED_LEVEL", level + 1);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Hardware back button goes to Level Select
        Intent intent = new Intent(GameOverActivity.this, LevelSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}