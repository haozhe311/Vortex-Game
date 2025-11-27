package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ScoreLevelSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_level_select);

        setupScoreButton(R.id.btnScoreLvl1, 1);
        setupScoreButton(R.id.btnScoreLvl2, 2);
        setupScoreButton(R.id.btnScoreLvl3, 3);
        setupScoreButton(R.id.btnScoreLvl4, 4);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupScoreButton(int btnId, int level) {
        Button btn = findViewById(btnId);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(ScoreLevelSelectActivity.this, ScoreActivity.class);
            // Pass the specific level to filter the database
            intent.putExtra("LEVEL_FILTER", level);
            startActivity(intent);
        });
    }
}