package com.harrydmorgan.shoppinglist.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.DatabaseHelper;
import com.harrydmorgan.shoppinglist.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class ListDateAdapter extends RecyclerView.Adapter<ListDateAdapter.ListViewHolder> {
    private ArrayList<String> items;
    private final Context context;
    private final ClickListener click;

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public ListDateAdapter(Context context, ArrayList<String> items, ClickListener click) {
        this.context = context;
        this.items = items;
        this.click = click;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.text_date_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(DatabaseHelper.sdf.parse(items.get(position)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] days = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        holder.itemText.setText(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
        holder.dateText.setText(items.get(position));
        holder.root.setOnClickListener(view -> click.itemClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ListViewHolder extends RecyclerView.ViewHolder{

        TextView itemText;
        TextView dateText;
        View root;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.dayText);
            dateText = itemView.findViewById(R.id.dateText);
            root = itemView.getRootView();
        }
    }

    public interface ClickListener {
        void itemClick(int position);
    }
}
