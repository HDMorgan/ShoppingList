package com.harrydmorgan.shoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    ArrayList<String> checkedCat;
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

        DatabaseHelper db = new DatabaseHelper(view.getContext());
        db.addNewItem("Test item 2");

        listItems = new HashMap<>();


        ArrayList<String> categories = new ArrayList<>();
        categories.add("Main");
        categories.add("Second");
        categories.add("Add new...");

        categorySpinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_item, categories);
        categorySpinner.setAdapter(spinnerAdapter);


        ArrayList<String> arr1 = new ArrayList<>();
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");

        ArrayList<String> arr3 = new ArrayList<>();
        arr3.add("This");
        arr3.add("This");
        arr3.add("This");

        ArrayList<String> arr2 = new ArrayList<>();
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");

        checkedCat = new ArrayList<>();
        checkedCat.add("Main");
        checkedCat.add("Main");
        checkedCat.add("Main");
        checkedCat.add("Main");
        checkedCat.add("Main");
        checkedCat.add("Main");

        listItems.put("Main", arr1);
        listItems.put("Second", arr3);
        listItems.put("Checked", arr2);

        RecyclerView recyclerView = view.findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

//        ListAdapter adapter = new ListAdapter(view.getContext(), arr);
//        recyclerView.setAdapter(adapter);

        adapter = new SectionedRecyclerViewAdapter();

        ExpandableSection checkedSection = new ExpandableSection("Checked", listItems.get("Checked"), this, "Checked");
        checkedSection.setCheckedCategories(checkedCat);

        adapter.addSection(new ExpandableSection("Main", listItems.get("Main"), this, "Check"));
        adapter.addSection(new ExpandableSection("Second", listItems.get("Second"), this, "Check"));
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
                    String cat = categorySpinner.getSelectedItem().toString();
                    listItems.get(cat).add(txtAddNew.getText().toString());
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
            checkedCat.add(section.getTitle());
            section.removeItem(position);
            adapter.notifyItemRemoved(itemAdapterPosition);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
//
        return;
    }
}