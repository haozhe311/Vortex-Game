package com.example.vortex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private int finishedLevel;
    private int totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Get Data
        finishedLevel = getIntent().getIntExtra("FINISHED_LEVEL", 1);
        int levelScore = getIntent().getIntExtra("LEVEL_SCORE", 0);
        totalScore = getIntent().getIntExtra("TOTAL_SCORE", 0);

        // Setup UI
        TextView tvTitle = findViewById(R.id.tvGameOverTitle);
        TextView tvSub = findViewById(R.id.tvGameOverSub);

        // Change title: "LEVEL 1 COMPLETE"
        tvTitle.setText("LEVEL " + finishedLevel + " COMPLETE");
        tvSub.setText("TOTAL SCORE: " + totalScore + "\n(+" + levelScore + " this level)");

        Button btnEndGame = findViewById(R.id.btnEndGame);
        Button btnNext = findViewById(R.id.btnNextLevel);

        // --- BUTTON LOGIC ---

        // "End Game" Button -> Checks High Score -> Main Menu
        btnEndGame.setOnClickListener(v -> checkHighScoreAndFinish());

        // "Next Level" Button -> Continues Game
        if (finishedLevel >= 4) {
            // If Level 4 is done, you CANNOT go next.
            btnNext.setVisibility(View.GONE);
            btnEndGame.setText("FINISH GAME"); // Rename End button
        } else {
            btnNext.setOnClickListener(v -> {
                Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
                intent.putExtra("SELECTED_LEVEL", finishedLevel + 1);
                intent.putExtra("ACCUMULATED_SCORE", totalScore); // Pass total score forward
                startActivity(intent);
                finish();
            });
        }
    }

    private void checkHighScoreAndFinish() {
        DBHelper db = new DBHelper(this);
        // Check if Total Score is in Top 25 for the level they finished at
        if (db.isTop25(totalScore, finishedLevel)) {
            showSaveDialog(db);
        } else {
            goToMainMenu();
        }
    }

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

            // Save: Name, Total Score, Level Reached
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

    @Override
    public void onBackPressed() {
        // Pressing back here acts like "End Game" (without saving score if you want, or with saving)
        // Usually, back just quits. Let's make it go to Main Menu safely.
        goToMainMenu();
    }
}