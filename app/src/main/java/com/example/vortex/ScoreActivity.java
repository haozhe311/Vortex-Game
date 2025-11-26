package com.example.vortex;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

/**
 * Purpose: Displays the persistence Top 25 high scores.
 * Fetches data from SQLite and populates a ListView.
 */
public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        ListView listView = findViewById(R.id.listViewScores);
        DBHelper db = new DBHelper(this);

        // Fetch scores from database
        ArrayList<String> scores = db.getTop25Scores();

        // Bind data to the ListView using a custom layout for styling
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_score, // Custom text view for cyberpunk style
                R.id.tvScoreItem,
                scores
        );

        listView.setAdapter(adapter);
    }
}