package com.harrydmorgan.shoppinglist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "com.harrydmorgan.shoppinglist/HistoryProvider";
    public static final String URL = "content://" + PROVIDER_NAME + "/history";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        DatabaseHelper helper = new DatabaseHelper(getContext());
        db = helper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String where, @Nullable String[] args, @Nullable String order) {
        return db.query("history", columns, where, args, null, null, order);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.example.history";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        db.insert("history", null, contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] args) {
        int count = db.delete("history", where, args);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
