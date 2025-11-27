package com.example.vortex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/**
 * Database Helper for managing persistent storage of game scores.
 * This class handles SQLite creation, upgrades, and specific queries regarding
 * the Top 25 leaderboard requirement.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GameDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SCORES = "scores";

    // Column Constants
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
        // Drop older table if exists and re-create.
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
            onCreate(db);
        }
    }

    /**
     * Inserts a new score entry into the database.
     *
     * @param name  The player's name.
     * @param score The total score achieved.
     * @param level The level reached or completed.
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
     * Retrieves the top 25 scores, optionally filtered by level.
     *
     * @param levelFilter The specific level to filter by, or -1 for all levels.
     * @return A list of formatted strings representing the leaderboard rows.
     */
    public ArrayList<String> getTop25Scores(int levelFilter) {
        ArrayList<String> scoreList = new ArrayList<>();
        String selectQuery;

        if (levelFilter == -1) {
            // Fetch top 25 across all levels
            selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";
        } else {
            // Fetch top 25 for a specific level
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
     * Determines if a given score qualifies for the Top 25.
     *
     * @param score The score achieved by the player.
     * @param level The level context.
     * @return True if the table has fewer than 25 entries OR the score beats the 25th lowest score.
     */
    public boolean isTop25(int score, int level) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 1. Check current count of rows
        String countQuery = "SELECT count(*) FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + level;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);

        if (count < 25) {
            cursor.close();
            return true; // Auto-qualify if list isn't full
        }

        // 2. If full, find the score of the 25th person
        String minQuery = "SELECT " + KEY_SCORE + " FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + level + " ORDER BY " + KEY_SCORE + " DESC LIMIT 1 OFFSET 24";
        Cursor minCursor = db.rawQuery(minQuery, null);

        if (minCursor.moveToFirst()) {
            int lowestTopScore = minCursor.getInt(0);
            minCursor.close();
            cursor.close();
            // Qualify if we beat the lowest score on the board
            return score > lowestTopScore;
        }

        cursor.close();
        return false;
    }
}