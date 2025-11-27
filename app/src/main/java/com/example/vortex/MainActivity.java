package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The entry point for the VORTEX application.
 * This activity handles the main menu UI and navigation to:
 * 1. The Game (starting at Level 1).
 * 2. The Help/Mission dialog.
 * 3. The Leaderboard/High Scores screen.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStartGame);
        Button btnHelp = findViewById(R.id.btnHelp);
        Button btnScores = findViewById(R.id.btnHighScores);

        // Launches the GameActivity, initializing it at Level 1.
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("SELECTED_LEVEL", 1);
            startActivity(intent);
        });

        // Displays the mission objective/help dialog to the user.
        btnHelp.setOnClickListener(v -> showHelpDialog());

        // Navigates to the ScoreActivity to view the global leaderboard.
        // Passing -1 indicates we want to see scores from all levels combined.
        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
            intent.putExtra("LEVEL_FILTER", -1);
            startActivity(intent);
        });
    }

    /**
     * Inflates and displays a custom alert dialog containing the game rules.
     * The background is set to transparent to accommodate the custom XML shape.
     */
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