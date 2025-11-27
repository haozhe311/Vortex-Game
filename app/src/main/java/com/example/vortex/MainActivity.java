package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Purpose: Main Entry Point.
 * UPDATED: Leaderboard button goes directly to the Score List.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStartGame);
        Button btnHelp = findViewById(R.id.btnHelp);
        Button btnScores = findViewById(R.id.btnHighScores);

        // 1. Start Game -> Level 1
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("SELECTED_LEVEL", 1);
            startActivity(intent);
        });

        // 2. Help Dialog
        btnHelp.setOnClickListener(v -> showHelpDialog());

        // 3. Leaderboard -> Direct to ScoreActivity (Global List)
        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
            intent.putExtra("LEVEL_FILTER", -1); // -1 indicates "Show All / Accumulated"
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