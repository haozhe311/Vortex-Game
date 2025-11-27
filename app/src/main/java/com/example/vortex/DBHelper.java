package com.example.vortex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/**
 * Purpose: Manages SQLite database for scores, including the Level played.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GameDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SCORES = "scores";

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

    public void addScore(String name, int score, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_SCORE, score);
        values.put(KEY_LEVEL, level);
        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    public ArrayList<String> getTop25Scores(int levelFilter) {
        ArrayList<String> scoreList = new ArrayList<>();
        String selectQuery;

        if (levelFilter == -1) {
            // Get All
            selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";
        } else {
            // Filter by Level
            selectQuery = "SELECT * FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + levelFilter + " ORDER BY " + KEY_SCORE + " DESC LIMIT 25";
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int rank = 1; // Counter for ranking
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCORE));
                int lvl = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL));

                // UPDATED FORMAT: Use Rank (1, 2, 3...) instead of LVL X
                if (levelFilter != -1) {
                    // Specific Level View: "1 | Agent : 6"
                    scoreList.add(rank + " | " + name + " : " + score);
                } else {
                    // Global View: "1 | Agent : 6 (Lvl 2)"
                    scoreList.add(rank + " | " + name + " : " + score + " (Lvl " + lvl + ")");
                }
                rank++; // Increment rank
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return scoreList;
    }

    public boolean isTop25(int score, int level) {
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT count(*) FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + level;
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);

        if (count < 25) {
            cursor.close();
            return true;
        }

        String minQuery = "SELECT " + KEY_SCORE + " FROM " + TABLE_SCORES + " WHERE " + KEY_LEVEL + "=" + level + " ORDER BY " + KEY_SCORE + " DESC LIMIT 1 OFFSET 24";
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