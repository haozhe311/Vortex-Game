package com.example.vortex;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

/**
 * Activity responsible for displaying the High Scores / Hall of Fame.
 * It uses a ListView to present the data fetched from the SQLite database.
 */
public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        ListView listView = findViewById(R.id.listViewScores);
        TextView tvSub = findViewById(R.id.tvLevelSub);

        DBHelper db = new DBHelper(this);

        // Retrieve the filter flag passed from MainActivity.
        // -1 implies a global view, otherwise it is specific to a level ID.
        int levelFilter = getIntent().getIntExtra("LEVEL_FILTER", -1);

        // Set the subtitle text based on the filter context.
        if (levelFilter == -1) {
            tvSub.setText("HALL OF FAME");
        } else {
            tvSub.setText("LEVEL " + levelFilter);
        }

        // Fetch the list of strings representing the Top 25 scores.
        ArrayList<String> scores = db.getTop25Scores(levelFilter);

        // Bind the data to the ListView using a standard ArrayAdapter.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_score,
                R.id.tvScoreItem,
                scores
        );

        listView.setAdapter(adapter);
    }
}