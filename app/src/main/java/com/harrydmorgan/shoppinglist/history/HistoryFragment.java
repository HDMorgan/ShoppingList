package com.harrydmorgan.shoppinglist.history;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.AppHelp;
import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    ListDateAdapter adapter;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.pageAction)
                .setTitle("Clear old data");
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        //Deleting old data
        if (itemId == R.id.pageAction) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Are you sure?")
                    .setMessage("This will delete all records over 3 months old")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        DatabaseHelper db = new DatabaseHelper(getContext());
                        db.clearOldHistory();
                        adapter.setItems(db.getHistoryDates());
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        if (itemId == R.id.help) {
            Intent intent = new Intent(getContext(), AppHelp.class);
            intent.putExtra("url", "history.html");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        DatabaseHelper db = new DatabaseHelper(getContext());

        //Setting up recyclerview
        RecyclerView historyRec = view.findViewById(R.id.history_recycler);
        ArrayList<String> dates = db.getHistoryDates();
        adapter = new ListDateAdapter(getContext(), dates, position -> {
            Intent intent = new Intent(getContext(), ShopActivity.class);
            intent.putExtra("date", dates.get(position));
            startActivity(intent);
        });
        historyRec.setAdapter(adapter);
        historyRec.setLayoutManager(new LinearLayoutManager(view.getContext()));

        return view;
    }
}