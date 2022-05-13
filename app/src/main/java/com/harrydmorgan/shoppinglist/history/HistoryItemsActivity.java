package com.harrydmorgan.shoppinglist.history;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.AppHelp;
import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.ExpandableSection;
import com.harrydmorgan.shoppinglist.HistoryProvider;
import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class HistoryItemsActivity extends AppCompatActivity implements ExpandableSection.ClickListener {
    private double geo[];
    SectionedRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_items);

        adapter = new SectionedRecyclerViewAdapter();

        long id = getIntent().getLongExtra("shop", 0);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        HashMap<String, ArrayList<String>> items = new HashMap<>();

        Cursor c = getContentResolver().query(HistoryProvider.CONTENT_URI,
                new String[] {"name", "category"},
                "locationId = ?",
                new String[] {Long.toString(id)},
                null);
        if (c.moveToFirst()) {
            do {
                String cat = c.getString(1);
                if (!items.containsKey(cat)) {
                    items.put(cat, new ArrayList<>());
                }
                items.get(cat).add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();

        geo = dbHelper.getGeo(id);

        for (String i : items.keySet()) {
            adapter.addSection(new ExpandableSection(i, items.get(i), this, null));
        }

        RecyclerView recyclerView = findViewById(R.id.historyItemRecycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getIntent().getStringExtra("name"));
        ab.setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.pageAction)
                .setIcon(R.drawable.ic_location)
                .setTitle("Open location");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();// Respond to the action bar's Up/Home button
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (itemId == R.id.pageAction) {
            Uri uri = Uri.parse("geo:0,0?q=" + geo[0] + "," + geo[1]);
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(geoIntent);
            return true;
        }
        if (itemId == R.id.help) {
            Intent intent = new Intent(this, AppHelp.class);
            intent.putExtra("url", "history.html#location");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHeaderRootViewClicked(@NonNull ExpandableSection section) {
        final SectionAdapter sectionAdapter = adapter.getAdapterForSection(section);

        // store info of current section state before changing its state
        final boolean wasExpanded = section.isExpanded();
        final int previousItemsTotal = section.getContentItemsTotal();

        section.setExpanded(!wasExpanded);
        sectionAdapter.notifyHeaderChanged();

        if (wasExpanded) {
            sectionAdapter.notifyItemRangeRemoved(0, previousItemsTotal);
        } else {
            sectionAdapter.notifyAllItemsInserted();
        }
    }

    @Override
    public void onItemRootViewClicked(@NonNull ExpandableSection section, int itemAdapterPosition) {

    }

    @Override
    public void clearButtonClicked() {

    }
}