package com.harrydmorgan.shoppinglist.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.ListAdapter;
import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    public HistoryFragment() {
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        DatabaseHelper db = new DatabaseHelper(getContext());

        RecyclerView historyRec = view.findViewById(R.id.history_recycler);
        ArrayList<String> dates = db.getHistoryDates();
        ListAdapter adapter = new ListAdapter(getContext(), dates, position -> {
            Intent intent = new Intent(getContext(), ShopActivity.class);
            intent.putExtra("date", dates.get(position));
            startActivity(intent);
        });
        historyRec.setAdapter(adapter);
        historyRec.setLayoutManager(new LinearLayoutManager(view.getContext()));

        historyRec.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        return view;
    }
}