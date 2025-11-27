package com.example.vortex;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        ListView listView = findViewById(R.id.listViewScores);
        TextView tvSub = findViewById(R.id.tvLevelSub); // NEW: Subtitle TextView

        DBHelper db = new DBHelper(this);

        // Get filter from intent (-1 means all, 1 means Level 1, etc.)
        int levelFilter = getIntent().getIntExtra("LEVEL_FILTER", -1);

        // Update Subtitle based on filter
        if (levelFilter == -1) {
            tvSub.setText("GLOBAL RANKING");
        } else {
            tvSub.setText("LEVEL " + levelFilter);
        }

        ArrayList<String> scores = db.getTop25Scores(levelFilter);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_score,
                R.id.tvScoreItem,
                scores
        );

        listView.setAdapter(adapter);
    }
}