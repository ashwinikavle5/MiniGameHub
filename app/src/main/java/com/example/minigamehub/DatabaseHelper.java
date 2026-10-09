package com.example.minigamehub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "MiniGameHub.db";

    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS =
            "users";

    private static final String TABLE_SCORES =
            "scores";


    public DatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // USERS TABLE

        String createUsersTable =
                "CREATE TABLE " +
                        TABLE_USERS +
                        " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL)";

        db.execSQL(createUsersTable);


        // SCORES TABLE

        String createScoresTable =
                "CREATE TABLE " +
                        TABLE_SCORES +
                        " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "game TEXT NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "played_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

        db.execSQL(createScoresTable);
    }


    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {

            String createScoresTable =
                    "CREATE TABLE " +
                            TABLE_SCORES +
                            " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT NOT NULL, " +
                            "game TEXT NOT NULL, " +
                            "score INTEGER NOT NULL, " +
                            "played_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

            db.execSQL(createScoresTable);
        }
    }


    // =========================
    // ADD USER
    // =========================

    public boolean addUser(
            String name,
            String password) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                name
        );

        values.put(
                "password",
                password
        );

        long result =
                db.insert(
                        TABLE_USERS,
                        null,
                        values
                );

        return result != -1;
    }


    // =========================
    // CHECK LOGIN
    // =========================

    public boolean checkUser(
            String name,
            String password) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM " +
                                TABLE_USERS +
                                " WHERE name = ? " +
                                "AND password = ?",
                        new String[]{
                                name,
                                password
                        }
                );

        boolean exists =
                cursor.moveToFirst();

        cursor.close();

        return exists;
    }


    // =========================
    // USER EXISTS
    // =========================

    public boolean userExists(
            String name) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM " +
                                TABLE_USERS +
                                " WHERE name = ?",
                        new String[]{
                                name
                        }
                );

        boolean exists =
                cursor.moveToFirst();

        cursor.close();

        return exists;
    }


    // =========================
    // SAVE GAME SCORE
    // =========================

    public void saveScore(
            String username,
            String game,
            int score) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "username",
                username
        );

        values.put(
                "game",
                game
        );

        values.put(
                "score",
                score
        );

        db.insert(
                TABLE_SCORES,
                null,
                values
        );
    }


    // =========================
    // GET BEST SCORE
    // =========================

    public int getBestScore(
            String username,
            String game) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT MAX(score) " +
                                "FROM " +
                                TABLE_SCORES +
                                " WHERE username = ? " +
                                "AND game = ?",
                        new String[]{
                                username,
                                game
                        }
                );

        int bestScore = 0;

        if (cursor.moveToFirst()) {

            bestScore =
                    cursor.getInt(0);
        }

        cursor.close();

        return bestScore;
    }
}
