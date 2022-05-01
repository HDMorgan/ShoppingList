package com.harrydmorgan.shoppinglist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
    private TextView shopNameText;
    private TextView shopNameButton;
    private ShopLocation currentShop;
    private DatabaseHelper dbHelper;
    private ProgressBar geoProgress;
    private ArrayList<String> categories;
    private ArrayAdapter<String> spinnerAdapter;


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

        dbHelper = new DatabaseHelper(view.getContext());

        currentShop = dbHelper.getLastShop();


        categories = dbHelper.getCategories(DatabaseHelper.LIST_TABLE, true);


        adapter = new SectionedRecyclerViewAdapter();
        for (String i : categories) {
            listItems.put(i, new ArrayList<>());
        }
        listItems.put("Checked", new ArrayList<>());

        categorySpinner = view.findViewById(R.id.category_spinner);
        categories.add("Add new...");
        spinnerAdapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, categories);
        categorySpinner.setAdapter(spinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (categorySpinner.getSelectedItem().toString().equals("Add new...")) {
                    TextDialog dialog = new TextDialog("Add category", new TextDialog.TextDialogListener() {
                        @Override
                        public void setAction(String textEntered) {
                            addItemCategory(textEntered);
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
                dbHelper.deleteItem(section.getItem(i), section.getTitle());
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

        shopNameText = view.findViewById(R.id.shop_name_text);
        shopNameButton = view.findViewById(R.id.shop_name_button);
        geoProgress = view.findViewById(R.id.geoProgress);

        if (currentShop != null) {
            shopNameText.setText("At " + currentShop.getName());
            shopNameButton.setText("Clear");
        }

        shopNameButton.setOnClickListener(view -> {
            if (currentShop == null) {
                TextDialog shopDialog = new TextDialog("Set shop", new TextDialog.TextDialogListener() {
                    @Override
                    public void setAction(String textEntered) {
                        if (!textEntered.equals("")) {
                            shopNameButton.setText("");
                            geoProgress.setVisibility(View.VISIBLE);
                            setLocation(textEntered);
                        }
                    }

                    @Override
                    public void cancelAction() {
                    }
                });
                shopDialog.show(getParentFragmentManager(), "tag");
            } else {
                currentShop = null;
                shopNameText.setText("");
                shopNameButton.setText("Set shop");
            }
        });

        //Hint dialog explaining how items are recorded
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getBoolean("showHint", true)) {
            View checkView = inflater.inflate(R.layout.checkbox, null);
            CheckBox checkBox = checkView.findViewById(R.id.dialog_check);
            new AlertDialog.Builder(getContext())
                    .setTitle("Hint: Shops")
                    .setMessage("In order to save the items you check off into history, you need to set the shop you are in. Otherwise the checked items will not be recorded")
                    .setView(checkView)
                    .setNeutralButton("OK", (dialogInterface, i) -> {
                        if (checkBox.isChecked()) {
                            editor.putBoolean("showHint", false);
                            editor.apply();
                        }
                    })
                    .show();
        }

        return view;
    }

    private void setLocation(String shopName) {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission error")
                    .setMessage("In order to set your shop, this app needs to have permissions for location.\nYou can change this in your phones app settings")
                    .setNeutralButton("OK", null)
                    .show();
            return;
        }
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                geoProgress.setVisibility(View.GONE);
                currentShop = dbHelper.getNewShop(shopName, location.getLatitude(), location.getLongitude());
                shopNameText.setText("At " + shopName);
                shopNameButton.setText("Clear");
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestSingleUpdate(criteria, locationListener, null);
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
            String item = section.getItem(position);
        if (section.getTitle().equals("Checked")) {
            String category = section.getCheckedCategory(position);
            if (!listItems.keySet().contains(category)) {
                addItemCategory(category);
            }
            dbHelper.uncheckItem(item, category);
            listItems.get(category).add(item);
            section.removeItem(position);
            adapter.notifyItemRemoved(itemAdapterPosition);
            adapter.notifyDataSetChanged();

        } else {
            Long id;
            if (currentShop != null) {
                id = currentShop.getId();
            } else {
                id = (long) -1;
            }
            listItems.get("Checked").add(section.getItem(position));
            dbHelper.checkItem(item, section.getTitle(), id);
            checkedCategories.add(section.getTitle());
            section.removeItem(position);
            adapter.notifyItemRemoved(itemAdapterPosition);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
//
        return;
    }

    private void addItemCategory(String title) {
        listItems.put(title, new ArrayList<>());
        int sectionIndex = adapter.getSectionCount() - 1;
        adapter.addSection(sectionIndex, new ExpandableSection(
                title,
                listItems.get(title),
                this,
                "item"
        ));
        categories.add(sectionIndex, title);
        spinnerAdapter.notifyDataSetChanged();
        categorySpinner.setSelection(sectionIndex);
    }
}