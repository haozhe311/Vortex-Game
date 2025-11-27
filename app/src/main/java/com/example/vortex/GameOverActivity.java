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
 * Displayed when a level is completed (timer runs out).
 * This activity:
 * 1. Shows the score summary.
 * 2. Determines if the user can proceed to the next level.
 * 3. Checks if the final score qualifies for the Top 25 leaderboard.
 */
public class GameOverActivity extends AppCompatActivity {

    private int finishedLevel;
    private int totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Retrieve score and level data from the ended game session.
        finishedLevel = getIntent().getIntExtra("FINISHED_LEVEL", 1);
        int levelScore = getIntent().getIntExtra("LEVEL_SCORE", 0);
        totalScore = getIntent().getIntExtra("TOTAL_SCORE", 0);

        TextView tvTitle = findViewById(R.id.tvGameOverTitle);
        TextView tvSub = findViewById(R.id.tvGameOverSub);

        tvTitle.setText("LEVEL " + finishedLevel + " COMPLETE");
        tvSub.setText("TOTAL SCORE: " + totalScore + "\n(+" + levelScore + " this level)");

        Button btnEndGame = findViewById(R.id.btnEndGame);
        Button btnNext = findViewById(R.id.btnNextLevel);

        btnEndGame.setOnClickListener(v -> checkHighScoreAndFinish());

        // Logic to determine if "Next Level" button should be shown.
        // The game ends after Level 4.
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
     * Checks the database to see if the user's score qualifies for the Hall of Fame.
     * If yes, prompts for a name; otherwise, returns to Main Menu.
     */
    private void checkHighScoreAndFinish() {
        DBHelper db = new DBHelper(this);

        if (db.isTop25(totalScore, finishedLevel)) {
            showSaveDialog(db);
        } else {
            goToMainMenu();
        }
    }

    /**
     * Displays a dialog allowing the winner to enter their name.
     *
     * @param db The database helper instance to perform the insertion.
     */
    private void showSaveDialog(DBHelper db) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("NEW HIGH SCORE!");
        builder.setMessage("Enter Name:");

        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setCancelable(false);

        builder.setPositiveButton("SUBMIT", (dialog, which) -> {
            String name = input.getText().toString();
            if (name.isEmpty()) name = "Guest";

            db.addScore(name, totalScore, finishedLevel);
            goToMainMenu();
        });
        builder.show();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}