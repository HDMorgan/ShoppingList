package com.harrydmorgan.shoppinglist.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private ArrayList<String> items;
    private Context context;
    private ClickListener click;

    public ListAdapter(Context context, ArrayList<String> items, ClickListener click) {
        this.context = context;
        this.items = items;
        this.click = click;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.text_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.itemText.setText(items.get(position));
        holder.root.setOnClickListener(view -> click.itemClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ListViewHolder extends RecyclerView.ViewHolder{

        TextView itemText;
        View root;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
            root = itemView.getRootView();
        }
    }

    public interface ClickListener {
        public void itemClick(int position);
    }
}
