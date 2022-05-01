package com.harrydmorgan.shoppinglist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ListFragment extends Fragment implements ExpandableSection.ClickListener {

    private SectionedRecyclerViewAdapter adapter;
    private HashMap<String, ArrayList<String>> listItems;
    private View view;
    private ArrayList<String> checkedCategories;
    private Spinner categorySpinner;
    private FusedLocationProviderClient fusedLocationClient;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        listItems = new HashMap<>();
        checkedCategories = new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(view.getContext());

        ArrayList<String> categories = dbHelper.getCategories(DatabaseHelper.LIST_TABLE, true);

        adapter = new SectionedRecyclerViewAdapter();
        for (String i : categories) {
            listItems.put(i, new ArrayList<>());
        }
        listItems.put("Checked", new ArrayList<>());


        categorySpinner = view.findViewById(R.id.category_spinner);
        categories.add("Add new...");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, categories);
        categorySpinner.setAdapter(spinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (categorySpinner.getSelectedItem().toString().equals("Add new...")) {
                    TextDialog dialog = new TextDialog("Add category", new TextDialog.TextDialogListener() {
                        @Override
                        public void setAction(String textEntered) {
                            listItems.put(textEntered, new ArrayList<>());
                            int sectionIndex = adapter.getSectionCount() - 1;
                            addItemCategory(textEntered, sectionIndex);
                            categories.add(sectionIndex, textEntered);
                            spinnerAdapter.notifyDataSetChanged();
                            categorySpinner.setSelection(sectionIndex);

                        }

                        @Override
                        public void cancelAction() {
                            categorySpinner.setSelection(0);
                        }
                    });
                    dialog.show(getParentFragmentManager(), "Add cat");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        dbHelper.populateListHashmap(listItems, checkedCategories);

        //Populating recyclerview with Main at top and checked at bottom
        adapter.addSection(new ExpandableSection("Main", listItems.get("Main"), this, "Item"));
        for (String i : listItems.keySet()) {
            if (!(i.equals("Main") || i.equals("Checked"))) {
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
                boolean notSwipeable = viewHolder instanceof ExpandableSection.HeaderViewHolder ||
                        viewHolder instanceof ExpandableSection.CheckedItemViewHolder;
                if (notSwipeable) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        EditText txtAddNew = (EditText) view.findViewById(R.id.txtNewItem);
        txtAddNew.setOnEditorActionListener((textView, i, keyEvent) -> {
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
        });

        updateShop();

        //TODO dialog explaining history

        return view;
    }

    private void updateShop() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission error")
                    .setMessage("To track the shops you visit, you need to allow location permissions for this app. You can do this in your phones app settings")
                    .setNeutralButton("OK", null)
                    .show();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                    }
                });
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

    private void addItemCategory(String title, int position) {
        adapter.addSection(position, new ExpandableSection(
                title,
                listItems.get(title),
                this,
                "item"
        ));
    }
}