package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Handles the logic when a level is completed or the game ends.
 * This activity displays the final score and determines if the user
 * should be prompted to enter their name for the leaderboard.
 */
public class GameOverActivity extends AppCompatActivity {

    private int finishedLevel;
    private int totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Retrieve data passed from GameActivity
        finishedLevel = getIntent().getIntExtra("FINISHED_LEVEL", 1);
        int levelScore = getIntent().getIntExtra("LEVEL_SCORE", 0);
        totalScore = getIntent().getIntExtra("TOTAL_SCORE", 0);

        // Bind UI Elements
        TextView tvTitle = findViewById(R.id.tvGameOverTitle);
        TextView tvSub = findViewById(R.id.tvGameOverSub);
        Button btnEndGame = findViewById(R.id.btnEndGame);
        Button btnNext = findViewById(R.id.btnNextLevel);

        // Update Text
        tvTitle.setText("LEVEL " + finishedLevel + " COMPLETE");
        tvSub.setText("TOTAL SCORE: " + totalScore + "\n(+" + levelScore + " this level)");

        // "End Game" Button: Checks score qualification before exiting
        btnEndGame.setOnClickListener(v -> checkHighScoreAndFinish());

        // "Next Level" Button: Only visible if not at the final level (Level 4)
        if (finishedLevel >= 4) {
            btnNext.setVisibility(View.GONE);
            btnEndGame.setText("FINISH GAME");
        } else {
            btnNext.setOnClickListener(v -> {
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtra("SELECTED_LEVEL", finishedLevel + 1);
                intent.putExtra("ACCUMULATED_SCORE", totalScore);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Checks if the user's score qualifies for the Hall of Fame.
     * If it qualifies, show the dialog. If not, go directly to Main Menu.
     */
    private void checkHighScoreAndFinish() {
        DBHelper db = new DBHelper(this);

        if (db.isTop25(totalScore, finishedLevel)) {
            showSaveDialog(db);
        } else {
            // Score is too low or list is full; return to menu immediately.
            goToMainMenu();
        }
    }

    /**
     * Shows a dialog for the user to enter their name.
     *
     * @param db The database helper instance.
     */
    private void showSaveDialog(DBHelper db) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("NEW HIGH SCORE!");
        builder.setMessage("Enter Name:");

        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setCancelable(false); // Prevents clicking outside to close

        builder.setPositiveButton("SUBMIT", (dialog, which) -> {
            String name = input.getText().toString();
            if (name.isEmpty()) name = "Guest";

            db.addScore(name, totalScore, finishedLevel);
            goToMainMenu();
        });

        builder.show();
    }

    /**
     * Navigates back to the Main Menu, clearing the activity stack.
     */
    private void goToMainMenu() {
        Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}