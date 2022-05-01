package com.harrydmorgan.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShoppingList.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public static final String LIST_TABLE = "list";
    public static final String HISTORY_TABLE = "history";
    public static final String LOCATIONS_TABLE = "locations";
    public static final String COLLECTIONS_TABLE = "collections";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());



    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String locationsTable = "CREATE TABLE locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "date DATE," +
                "latitude FLOAT," +
                "longitude FLOAT);";
        db.execSQL(locationsTable);

        String listTable = "CREATE TABLE list (" +
                "name TEXT," +
                "category TEXT, " +
                "locationId int);";
        db.execSQL(listTable);

        String collectionsTable = "CREATE TABLE collections (" +
                "collection_name TEXT," +
                "item_name TEXT," +
                "category TEXT);";
        db.execSQL(collectionsTable);


        String historyTable = "CREATE TABLE history (" +
                "name TEXT," +
                "category TEXT, " +
                "locationId int," +
                "FOREIGN KEY (locationId) " +
                "REFERENCES locations(id) " +
                "ON DELETE CASCADE);";
        db.execSQL(historyTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public boolean addNewItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newItem = new ContentValues();
        newItem.put("category", category);
        newItem.put("name", item);
        long result = db.insert(LIST_TABLE, null, newItem);
        return result != -1;
    }

    public void populateListHashmap(HashMap<String, ArrayList<String>> items, ArrayList<String> checkedCategories) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM list;", null);
        if (c.moveToFirst()) {
            do {
                if (c.isNull(2)) {
                    items.get(c.getString(1))
                            .add(c.getString(0));
                } else {
                    items.get("Checked").add(c.getString(0));
                    checkedCategories.add(c.getString(1));
                }
            } while (c.moveToNext());
        }
        c.close();
    }

    public ArrayList<String> getCategories(String table, boolean requireMain) {
        ArrayList<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT category FROM list";
        if (requireMain) {
            query += " WHERE category != 'Main'";
            categories.add("Main");
        }
        query += ";";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                categories.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return categories;
    }

    public void checkItem(String item, String category, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE list SET " +
                "locationId = " + id +
                " WHERE name = '" + item +
                "' AND category = '" + category +
                "' AND locationId IS NULL;";
        db.execSQL(query);
    }

    public void uncheckItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE list SET " +
                "locationId = NULL" +
                " WHERE name = '" + item +
                "' AND category = '" + category +
                "' AND locationId NOT NULL;";
        db.execSQL(query);
    }

    public ShopLocation getNewShop(String shopName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", shopName);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        Date currentDate = new Date();
        cv.put("date", sdf.format(currentDate));
        long id = db.insert("locations", null, cv);
        return new ShopLocation(id, shopName, currentDate, latitude, longitude);
    }

    public ShopLocation getLastShop() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM locations " +
                "ORDER BY id DESC " +
                "LIMIT 1;";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            Date date = null;
            try {
                date = sdf.parse(c.getString(2));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new ShopLocation(
                    c.getLong(0),
                    c.getString(1),
                    date,
                    c.getDouble(3),
                    c.getDouble(4)
            );
        }
        return null;
    }

    public void deleteItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM list WHERE " +
                "name = '" + item +
                "' AND category = '" + category +
                "' AND locationId IS NULL";
        db.execSQL(query);
    }
}
