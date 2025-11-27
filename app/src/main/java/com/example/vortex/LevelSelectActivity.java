package com.example.vortex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Purpose: Allows user to choose a level.
 * Logic: Checks 'unlocked_level' in SharedPreferences to enable/disable buttons.
 */
public class LevelSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        // Load progress
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int maxUnlocked = prefs.getInt("unlocked_level", 1); // Default Level 1 is unlocked

        setupLevelButton(R.id.btnLevel1, 1, maxUnlocked);
        setupLevelButton(R.id.btnLevel2, 2, maxUnlocked);
        setupLevelButton(R.id.btnLevel3, 3, maxUnlocked);
        setupLevelButton(R.id.btnLevel4, 4, maxUnlocked);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh unlocked levels safely without calling lifecycle methods directly
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int maxUnlocked = prefs.getInt("unlocked_level", 1);

        setupLevelButton(R.id.btnLevel1, 1, maxUnlocked);
        setupLevelButton(R.id.btnLevel2, 2, maxUnlocked);
        setupLevelButton(R.id.btnLevel3, 3, maxUnlocked);
        setupLevelButton(R.id.btnLevel4, 4, maxUnlocked);
    }

    private void setupLevelButton(int btnId, int level, int maxUnlocked) {
        Button btn = findViewById(btnId);

        if (level <= maxUnlocked) {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
            btn.setText("LEVEL " + level);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(LevelSelectActivity.this, GameActivity.class);
                intent.putExtra("SELECTED_LEVEL", level);
                startActivity(intent);
            });
        } else {
            // Locked State
            btn.setEnabled(false);
            btn.setAlpha(0.3f); // Dimmed
            btn.setText("LOCKED");
        }
    }
}