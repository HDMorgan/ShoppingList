package com.harrydmorgan.shoppinglist;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ListFragment extends Fragment implements ExpandableSection.ClickListener {

    SectionedRecyclerViewAdapter adapter;
    HashMap<String, ArrayList<String>> listItems;
    View view;
    ArrayList<String> checkedCategories;
    Spinner categorySpinner;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list, container, false);

        listItems = new HashMap<>();
        checkedCategories = new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(view.getContext());

        ArrayList<String> categories = dbHelper.getCategories(DatabaseHelper.LIST_TABLE, true);

        adapter = new SectionedRecyclerViewAdapter();
        for (String i : categories) {
            listItems.put(i, new ArrayList<String>());
        }
        listItems.put("Checked", new ArrayList<String>());


        categorySpinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_item, categories);
        categorySpinner.setAdapter(spinnerAdapter);


        RecyclerView recyclerView = view.findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        dbHelper.populateListHashmap(listItems, checkedCategories);

        //Populating recyclerview with Main at top and checked at bottom
        adapter.addSection(new ExpandableSection("Main", listItems.get("Main"), this, "Item"));
        for (String i : listItems.keySet()) {
            if (! (i.equals("Main") || i.equals("Checked"))) {
                adapter.addSection(new ExpandableSection(i, listItems.get(i), this, "Item"));
            }
        }
        ExpandableSection checkedSection = new ExpandableSection("Checked", listItems.get("Checked"), this, "Checked");
        checkedSection.setCheckedCategories(checkedCategories);

        adapter.addSection(checkedSection);
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
                section.removeItem(i);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ExpandableSection.HeaderViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        EditText txtAddNew = (EditText) view.findViewById(R.id.txtNewItem);
        txtAddNew.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    String category = categorySpinner.getSelectedItem().toString();
                    String item = txtAddNew.getText().toString();
                    listItems.get(category).add(item);
                    dbHelper.addNewItem(item, category);
                    adapter.notifyDataSetChanged();
                    txtAddNew.setText("");
                    return true;
                }
                return false;
            }
        });

        return view;
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
        int position = adapter.getPositionInSection(itemAdapterPosition);
        if (section.getTitle().equals("Checked")) {
            String category = section.getCheckedCategory(position);
            listItems.get(category).add(section.getItem(position));
            section.removeItem(position);
            adapter.notifyItemRemoved(itemAdapterPosition);
            adapter.notifyDataSetChanged();

        } else {
            listItems.get("Checked").add(section.getItem(position));
            checkedCategories.add(section.getTitle());
            section.removeItem(position);
            adapter.notifyItemRemoved(itemAdapterPosition);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
//
        return;
    }
}