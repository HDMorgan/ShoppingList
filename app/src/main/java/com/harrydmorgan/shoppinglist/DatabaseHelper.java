package com.harrydmorgan.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShoppingList.db";
    private static final int DATABASE_VERSION = 1;

    public static final String LIST_TABLE = "list";
    public static final String HISTORY_TABLE = "history";
    public static final String LOCATIONS_TABLE = "locations";
    public static final String COLLECTIONS_TABLE = "collections";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating tables
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

    public void addNewItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newItem = new ContentValues();
        newItem.put("category", category);
        newItem.put("name", item);
        db.insert(LIST_TABLE, null, newItem);
    }

    //Populating the hashmap and checked arraylist for the main list
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

    //Get categories from given table
    public ArrayList<String> getCategories(String table, boolean requireMain) {
        ArrayList<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT category FROM " + table;
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

    //Check an item
    public void checkItem(String item, String category, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        item = item.replace("'", "''");
        category = category.replace("'", "''");
        String query = "UPDATE list SET " +
                "locationId = " + id +
                " WHERE name = '" + item +
                "' AND category = '" + category +
                "' AND locationId IS NULL;";
        db.execSQL(query);
    }

    //Uncheck an item
    public void uncheckItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        item = item.replace("'", "''");
        category = category.replace("'", "''");
        String query = "UPDATE list SET " +
                "locationId = NULL" +
                " WHERE name = '" + item +
                "' AND category = '" + category +
                "' AND locationId NOT NULL;";
        db.execSQL(query);
    }

    //Saving a new shop and returning its id
    public ShopLocation getNewShop(String shopName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Calendar c = Calendar.getInstance();
        shopName = shopName.replace("'", "''");
        cv.put("name", shopName);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("date", sdf.format(c.getTime()));
        long id = db.insert("locations", null, cv);
        return new ShopLocation(id, shopName, c, latitude, longitude);
    }

    //Get the last shop the user was at
    public ShopLocation getLastShop() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM locations " +
                "ORDER BY id DESC " +
                "LIMIT 1;";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(c.getString(2)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new ShopLocation(
                    c.getLong(0),
                    c.getString(1),
                    calendar,
                    c.getDouble(3),
                    c.getDouble(4)
            );
        }
        c.close();
        return null;
    }

    //Delete an item
    public void deleteItem(String item, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        item = item.replace("'", "''");
        category = category.replace("'", "''");
        String query = "DELETE FROM list WHERE " +
                "name = '" + item +
                "' AND category = '" + category +
                "' AND locationId IS NULL";
        db.execSQL(query);
    }

    //Save items to history and clear main list
    public void saveItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        String recordItems = "INSERT INTO history " +
                "SELECT * FROM list WHERE " +
                "locationId NOT NULL AND " +
                "locationId != -1;";
        String clearItems = "DELETE FROM list WHERE " +
                "locationid NOT NULL";
        db.execSQL(recordItems);
        db.execSQL(clearItems);
    }

    //Get dates from locations
    public ArrayList<String> getHistoryDates() {
        ArrayList<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT DISTINCT date FROM locations " +
                "ORDER BY date DESC;";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                dates.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return dates;
    }

    //Get names of shops for a given date
    public void getShopNames(String date, ArrayList<String> nameResults, ArrayList<Long> idResults) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id, name FROM locations " +
                "WHERE date = '" + date + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                idResults.add(c.getLong(0));
                nameResults.add(c.getString(1));
            } while (c.moveToNext());
        }
        c.close();
    }

    //Getting the location of a shop
    public double[] getGeo(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        double[] geo = new double[2];
        String query = "SELECT latitude, longitude FROM locations " +
                "WHERE id = " + id + ";";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            geo[0] = c.getDouble(0);
            geo[1] = c.getDouble(1);
        }
        c.close();
        return geo;
    }

    //Get the saved collections
    public ArrayList<String> getCollections() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> collections = new ArrayList<>();
        String query = "SELECT DISTINCT collection_name from collections;";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                collections.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return collections;
    }

    //populate the hashmap for a collection
    public void populateCollectionHashmap(HashMap<String, ArrayList<String>> listItems, String collectionName) {
        SQLiteDatabase db = this.getReadableDatabase();
        collectionName = collectionName.replace("'", "''");
        String query = "SELECT item_name, category FROM collections " +
                "WHERE collection_name = '" + collectionName + "';";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                listItems.get(c.getString(1)).add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
    }

    //Delete an item in a collection
    public void deleteCollectionItem(String item, String category, String collectionName) {
        SQLiteDatabase db = this.getWritableDatabase();
        item = item.replace("'", "''");
        category = category.replace("'", "''");
        collectionName = collectionName.replace("'", "''");
        String query = "DELETE FROM collections WHERE " +
                "item_name = '" + item +
                "' AND category = '" + category +
                "' AND collection_name = '" + collectionName + "';";
        db.execSQL(query);
    }

    //Delete a whole collection
    public void deleteCollection(String collectionName) {
        SQLiteDatabase db = this.getWritableDatabase();
        collectionName = collectionName.replace("'", "''");
        String query = "DELETE FROM collections WHERE " +
                "collection_name = '" + collectionName + "';";
        db.execSQL(query);
    }

    //Add an item to a collection
    public void addCollectionItem(String item, String category, String collection) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newItem = new ContentValues();
        newItem.put("category", category);
        newItem.put("item_name", item);
        newItem.put("collection_name", collection);
        db.insert(COLLECTIONS_TABLE, null, newItem);
    }

    //Insert a collection's items into the main list
    public void insertCollection(String collection) {
        SQLiteDatabase db = this.getWritableDatabase();
        collection = collection.replace("'", "''");
        String query = "INSERT INTO list (name, category) SELECT " +
                "c.item_name, c.category FROM collections c WHERE " +
                "c.collection_name = '" + collection +
                "' AND c.item_name NOT IN (SELECT l.name FROM list l WHERE l.category = c.category " +
                "AND locationId IS NULL);";
        db.execSQL(query);
    }

    //Delete a shop if it is cleared with no items checked
    public void removeShopIfEmpty(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String listQuery = "(SELECT locationId FROM list WHERE locationId NOT NULL)";
        String historyQuery = "(SELECT locationId FROM history)";
        String query = "DELETE FROM locations WHERE " +
                "id = " + id + " AND " +
                "id NOT IN " + listQuery + " AND " +
                "id NOT IN " + historyQuery + ";";
        db.execSQL(query);
    }

    //Removing old records from history - over 3 months
    public void clearOldHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM locations WHERE " +
                "julianday('now') - julianday(date) > 90;";
        db.execSQL(query);
    }
}

