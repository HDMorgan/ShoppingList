package com.harrydmorgan.shoppinglist.collections;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.R;
import com.harrydmorgan.shoppinglist.TextDialog;

import java.util.ArrayList;


public class CollectionsFragment extends Fragment {
    ArrayList<String> collections;
    CollectionsAdadpter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.pageAction)
                .setIcon(R.drawable.ic_add)
                .setTitle("Add collection");
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pageAction:
                TextDialog dialog = new TextDialog("Add collection", new TextDialog.TextDialogListener() {
                    @Override
                    public void setAction(String textEntered) {
                        if (textEntered.equals("") || collections.contains(textEntered)) {
                            return;
                        }
                        Intent intent = new Intent(getContext(), CollectionActivity.class);
                        intent.putExtra("collection", textEntered);
                        intent.putExtra("new", true);
                        startActivity(intent);
                    }

                    @Override
                    public void cancelAction() {}
                });
                dialog.show(getParentFragmentManager(), "Add collection");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collections, container, false);

        collections = new ArrayList<>();


        RecyclerView recyclerView = view.findViewById(R.id.collectionsRecycler);
        adapter = new CollectionsAdadpter(getContext(), collections, new CollectionsAdadpter.CollectionsListener() {
            @Override
            public void onRootClick(int position) {
                Intent intent = new Intent(getContext(), CollectionActivity.class);
                intent.putExtra("collection", collections.get(position));
                startActivity(intent);
            }

            @Override
            public void onAddClick(ImageView icon, int position) {
                icon.setImageResource(R.drawable.ic_check);
                icon.setOnClickListener(view1 -> {});
                DatabaseHelper db = new DatabaseHelper(getContext());
                db.insertCollection(collections.get(position));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        collections = dbHelper.getCollections();
        adapter.setCollections(collections);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}