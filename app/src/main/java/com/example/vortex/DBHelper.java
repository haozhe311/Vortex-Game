package com.example.vortex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/**
 * Purpose: Manages the SQLite database for persistent score storage.
 * Handles creating the database, adding scores, and retrieving the Top 25.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GameDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SCORES = "scores";

    // Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table query
        String createTable = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_SCORE + " INTEGER" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    /**
     * Inserts a new name and score into the database.
     * @param name Player's name.
     * @param score Player's final score.
     */
    public void addScore(String name, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_SCORE, score);
        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    /**
     * Retrieves the top 25 highest scores sorted in descending order.
     * @return An ArrayList of formatted strings (Name - Score).
     */
    public ArrayList<String> getTop25Scores() {
        ArrayList<String> scoreList = new ArrayList<>();
        // Query to get scores sorted by highest first, limited to 25
        String selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE));
                scoreList.add(name + " - " + score + " pts");
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return scoreList;
    }

    /**
     * Determines if a score qualifies for the Top 25 list.
     * Logic: Returns true if there are fewer than 25 entries OR
     * if the new score is higher than the lowest score currently in the list.
     * @param score The score to check.
     * @return True if score is high enough, False otherwise.
     */
    public boolean isTop25(int score) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Step 1: Check total number of records
        String countQuery = "SELECT count(*) FROM " + TABLE_SCORES;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);

        if (count < 25) {
            cursor.close();
            return true; // List is not full, so any score qualifies
        }

        // Step 2: Compare against the 25th (lowest) score in the high score list
        String minQuery = "SELECT " + KEY_SCORE + " FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT 1 OFFSET 24";
        Cursor minCursor = db.rawQuery(minQuery, null);

        if (minCursor.moveToFirst()) {
            int lowestTopScore = minCursor.getInt(0);
            minCursor.close();
            cursor.close();
            return score > lowestTopScore;
        }

        cursor.close();
        return false;
    }
}