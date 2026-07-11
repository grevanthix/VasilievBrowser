package ru.unknows.vasiliev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "browser_history.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_BOOKMARKED = "bookmarked";
    public static final String COLUMN_BOOKMARKED_AT = "bookmarked_at";

    public HistoryDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String makeTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_BOOKMARKED + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_BOOKMARKED_AT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(makeTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + COLUMN_BOOKMARKED + " INTEGER NOT NULL DEFAULT 0;");
        }
        if (oldV < 3) {
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + COLUMN_BOOKMARKED_AT + " INTEGER NOT NULL DEFAULT 0;");
        }
    }

    public void addRecord(String t, String u) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, t);
        cv.put(COLUMN_URL, u);
        cv.put(COLUMN_BOOKMARKED, 0);
        cv.put(COLUMN_BOOKMARKED_AT, 0);
        db.insert(TABLE_HISTORY, null, cv);
        db.close();
    }

    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        String order = COLUMN_BOOKMARKED + " DESC, " + COLUMN_BOOKMARKED_AT + " DESC, " + COLUMN_ID + " DESC";
        return db.query(TABLE_HISTORY, null, null, null, null, null, order);
    }

    public int toggleBookmark(long item_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[1];
        args[0] = String.valueOf(item_id);
        
        Cursor cur = db.query(TABLE_HISTORY, new String[]{COLUMN_BOOKMARKED}, COLUMN_ID + " = ?", args, null, null, null);
        int curVal = 0;
        
        if (cur != null) {
            if (cur.moveToFirst() == true) {
                curVal = cur.getInt(0);
            }
            cur.close();
        }
        
        int newV = 0;
        if (curVal == 0) {
            newV = 1;
        }
        
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BOOKMARKED, newV);
        if (newV == 1) {
            cv.put(COLUMN_BOOKMARKED_AT, System.currentTimeMillis());
        } else {
            cv.put(COLUMN_BOOKMARKED_AT, 0);
        }
        
        db.update(TABLE_HISTORY, cv, COLUMN_ID + " = ?", args);
        db.close();
        return newV;
    }

    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COLUMN_BOOKMARKED + " = 0", null);
        db.close();
    }

    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        db.close();
    }
}
