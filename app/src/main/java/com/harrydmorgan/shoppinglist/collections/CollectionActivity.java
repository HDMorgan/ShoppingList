package com.harrydmorgan.shoppinglist.collections;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.AppHelp;
import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.ExpandableSection;
import com.harrydmorgan.shoppinglist.R;
import com.harrydmorgan.shoppinglist.TextDialog;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class CollectionActivity extends AppCompatActivity implements ExpandableSection.ClickListener {


    private HashMap<String, ArrayList<String>> listItems;
    private ArrayList<String> categories;
    private SectionedRecyclerViewAdapter adapter;
    private Spinner categorySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private String collectionName;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.pageAction)
                .setIcon(R.drawable.ic_bin)
                .setTitle("Delete collection");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.pageAction:
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to delete this collection?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            DatabaseHelper dbHelper = new DatabaseHelper(this);
                            dbHelper.deleteCollection(collectionName);
                            onBackPressed();
                        })
                        .setNegativeButton("No", null)
                        .show();
            case R.id.help:
                Intent intent = new Intent(this, AppHelp.class);
                intent.putExtra("url", "collections.html#edit");
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        collectionName = getIntent().getStringExtra("collection");

        listItems = new HashMap<>();


        DatabaseHelper dbHelper = new DatabaseHelper(this);
        if (getIntent().getBooleanExtra("new", false)) {
            categories = new ArrayList<>();
            categories.add("Main");
        } else {
            categories = dbHelper.getCategories(DatabaseHelper.COLLECTIONS_TABLE, false);
        }

        adapter = new SectionedRecyclerViewAdapter();
        for (String i : categories) {
            listItems.put(i, new ArrayList<>());
        }

        categorySpinner = findViewById(R.id.category_spinner);
        categories.add("Add new...");
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, categories);
        categorySpinner.setAdapter(spinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (categorySpinner.getSelectedItem().toString().equals("Add new...")) {
                    TextDialog dialog = new TextDialog("Add category", new TextDialog.TextDialogListener() {
                        @Override
                        public void setAction(String textEntered) {
                            if (textEntered.equals("")) {
                                categorySpinner.setSelection(0);
                                return;
                            }
                            if (categories.contains(textEntered)) {
                                categorySpinner.setSelection(categories.indexOf(textEntered));
                                return;
                            }
                            addItemCategory(textEntered);
                        }

                        @Override
                        public void cancelAction() {
                            categorySpinner.setSelection(0);
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "Add collection cat");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        RecyclerView recyclerView = findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper.populateCollectionHashmap(listItems, collectionName);

        //Populating recyclerview
        for (String i : listItems.keySet()) {
            adapter.addSection(new ExpandableSection(i, listItems.get(i), this, null));
        }

        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ExpandableSection section = (ExpandableSection) adapter.getSectionForPosition(position);
                int i = adapter.getPositionInSection(position);
                dbHelper.deleteCollectionItem(section.getItem(i), section.getTitle(), collectionName);
                section.removeItem(i);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                boolean notSwipeable = viewHolder instanceof ExpandableSection.HeaderViewHolder ||
                        viewHolder instanceof ExpandableSection.CheckedItemViewHolder;
                if (notSwipeable) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        EditText txtAddNew = (EditText) findViewById(R.id.txtNewItem);
        txtAddNew.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                String category = categorySpinner.getSelectedItem().toString();
                String item = txtAddNew.getText().toString();
                txtAddNew.setText("");
                if (item.equals("") || listItems.get(category).contains(item)) {
                    return true;
                }
                listItems.get(category).add(item);
                dbHelper.addCollectionItem(item, category, collectionName);
                adapter.notifyDataSetChanged();
                return true;
            }
            return false;
        });

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(collectionName);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void addItemCategory(String title) {
        listItems.put(title, new ArrayList<>());
        int sectionIndex = adapter.getSectionCount();
        adapter.addSection( new ExpandableSection(
                title,
                listItems.get(title),
                this,
                null
        ));
        categories.add(sectionIndex, title);
        spinnerAdapter.notifyDataSetChanged();
        categorySpinner.setSelection(sectionIndex);
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