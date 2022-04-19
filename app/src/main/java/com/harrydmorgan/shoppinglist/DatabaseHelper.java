package com.harrydmorgan.shoppinglist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShoppingList.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;




    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String listTable = "CREATE TABLE list (" +
                "sub_list TEXT, " +
                "item_name TEXT);";
        db.execSQL(listTable);
        Toast.makeText(context, "Database stuff", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
