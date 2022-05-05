package com.harrydmorgan.shoppinglist.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.ListAdapter;
import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        DatabaseHelper db = new DatabaseHelper(this);
        RecyclerView recyclerView = findViewById(R.id.shop_name_recycler);
        String date = getIntent().getStringExtra("date");


        ArrayList<String> shops = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<>();
        db.getShopNames(date, shops, ids);
        ListAdapter adapter = new ListAdapter(this, shops, position -> {
            Intent intent = new Intent(this, HistoryItemsActivity.class);
            intent.putExtra("name", shops.get(position));
            intent.putExtra("shop", ids.get(position));
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Shops");
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}