package com.example.vortex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/**
 * Manages the SQLite database for game scores.
 * This class handles creating the database, upgrading it, and performing
 * CRUD operations for the leaderboard.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GameDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SCORES = "scores";

    // Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";
    private static final String KEY_LEVEL = "level";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_SCORE + " INTEGER,"
                + KEY_LEVEL + " INTEGER" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
            onCreate(db);
        }
    }

    /**
     * Adds a new high score entry to the database.
     *
     * @param name  The player's name.
     * @param score The score achieved.
     * @param level The level the score belongs to.
     */
    public void addScore(String name, int score, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_SCORE, score);
        values.put(KEY_LEVEL, level);
        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    /**
     * Retrieves the list of top 25 scores formatted for display.
     *
     * @param levelFilter The level to filter by, or -1 for all levels.
     * @return A list of strings representing the leaderboard ranks.
     */
    public ArrayList<String> getTop25Scores(int levelFilter) {
        ArrayList<String> scoreList = new ArrayList<>();
        String selectQuery;

        if (levelFilter == -1) {
            selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + levelFilter + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int rank = 1;
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE));
                int lvl = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL));

                if (levelFilter != -1) {
                    scoreList.add(rank + " | " + name + " : " + score);
                } else {
                    scoreList.add(rank + " | " + name + " : " + score + " (Lvl " + lvl + ")");
                }
                rank++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return scoreList;
    }

    /**
     * Checks if a score qualifies for the Top 25 leaderboard.
     * Logic:
     * 1. If the leaderboard has fewer than 25 entries for this level, ANY score qualifies.
     * 2. If the leaderboard is full (25 entries), the new score must be strictly greater
     * than the lowest (25th) score to qualify.
     * @param score The score the player just achieved.
     * @param level The level context.
     * @return True if the score should be added to the database.
     */
    public boolean isTop25(int score, int level) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean qualifies = false;

        // Fetch the current Top 25 specifically for this level
        String query = "SELECT " + KEY_SCORE + " FROM " + TABLE_SCORES +
                " WHERE " + KEY_LEVEL + "=? ORDER BY " + KEY_SCORE + " DESC LIMIT 25";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(level)});

        if (cursor.getCount() < 25) {
            // Case 1: The list isn't full yet. Always qualify.
            qualifies = true;
        } else {
            // Case 2: The list is full. Check the last entry (the 25th score).
            if (cursor.moveToLast()) {
                int lowestTopScore = cursor.getInt(0);
                // You must beat the 25th score to enter.
                if (score > lowestTopScore) {
                    qualifies = true;
                }
            }
        }

        cursor.close();
        return qualifies;
    }
}