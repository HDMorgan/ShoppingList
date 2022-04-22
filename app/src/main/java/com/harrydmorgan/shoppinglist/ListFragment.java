package com.harrydmorgan.shoppinglist;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ListFragment extends Fragment implements ExpandableSection.ClickListener {

    SectionedRecyclerViewAdapter adapter;
    HashMap<String, ArrayList<String>> listItems;

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
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        DatabaseHelper db = new DatabaseHelper(view.getContext());
        db.addNewItem("Test item 2");

        HashMap<String, ArrayList<String>> listItems = new HashMap<>();


        ArrayList<String> arr1 = new ArrayList<>();
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");
        arr1.add("One");

        ArrayList<String> arr2 = new ArrayList<>();
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");
        arr2.add("Two");

        listItems.put("main", arr1);
        listItems.put("checked", arr2);

        RecyclerView recyclerView = view.findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

//        ListAdapter adapter = new ListAdapter(view.getContext(), arr);
//        recyclerView.setAdapter(adapter);

        adapter = new SectionedRecyclerViewAdapter();

        adapter.addSection(new ExpandableSection("Main", arr1, this));
        adapter.addSection( new ExpandableSection("Checked", arr2, this));

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

                Toast.makeText(view.getContext(), section.getTitle() + i, Toast.LENGTH_SHORT).show();
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
//        txtAddNew.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_ACTION_NEXT) {
//                    adapter.addItem(txtAddNew.getText().toString());
//                    txtAddNew.setText("");
//                    return true;
//                }
//                return false;
//            }
//        });

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
        return;
    }
}