package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Purpose: Main Entry Point. Navigates to Level Select.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStartGame);
        Button btnHelp = findViewById(R.id.btnHelp);
        Button btnScores = findViewById(R.id.btnHighScores);

        // 1. Start Game -> Level Select
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LevelSelectActivity.class);
            startActivity(intent);
        });

        // 2. Help Dialog
        btnHelp.setOnClickListener(v -> showHelpDialog());

        // 3. Top 25 Scores -> UPDATED: Score Level Select
        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreLevelSelectActivity.class);
            startActivity(intent);
        });
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        Button btnAck = dialogView.findViewById(R.id.btnUnderstood);
        btnAck.setOnClickListener(view -> dialog.dismiss());
    }
}