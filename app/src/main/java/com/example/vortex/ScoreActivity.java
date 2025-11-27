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
        TextView tvSub = findViewById(R.id.tvLevelSub);

        DBHelper db = new DBHelper(this);

        // We passed -1 from MainActivity
        int levelFilter = getIntent().getIntExtra("LEVEL_FILTER", -1);

        // Update Subtitle
        if (levelFilter == -1) {
            tvSub.setText("HALL OF FAME");
        } else {
            tvSub.setText("LEVEL " + levelFilter);
        }

        // Fetch Top 25 (Global Accumulated)
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