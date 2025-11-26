package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        Button btnStart = findViewById(R.id.btnStartGame);
        Button btnHelp = findViewById(R.id.btnHelp);
        Button btnScores = findViewById(R.id.btnHighScores);

        // Navigate to the main Game Activity
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        // Show the Mission/Help dialog
        btnHelp.setOnClickListener(v -> showHelpDialog());

        // Navigate to the High Scores Activity
        btnScores.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Displays a custom alert dialog containing game instructions.
     */
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Inflate custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set background to transparent to allow rounded corners in XML to show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        // Handle the acknowledge button inside the dialog
        Button btnAck = dialogView.findViewById(R.id.btnUnderstood);
        btnAck.setOnClickListener(view -> dialog.dismiss());
    }
}