package com.harrydmorgan.shoppinglist.collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harrydmorgan.shoppinglist.R;

import java.util.ArrayList;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.CollectionsViewHolder> {
    private Context context;
    private ArrayList<String> collections;
    private CollectionsListener listener;

    public CollectionsAdapter(Context context, ArrayList<String> collections, CollectionsListener listener) {
        this.context = context;
        this.collections = collections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CollectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.collection_item, parent, false);
        return new CollectionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionsViewHolder holder, int position) {
        holder.text.setText(collections.get(position));
        holder.root.setOnClickListener(view -> listener.onRootClick(position));
        holder.icon.setOnClickListener(view -> listener.onAddClick(holder.icon, position));
    }

    @Override
    public int getItemCount() {return collections.size();}

    public static class CollectionsViewHolder extends RecyclerView.ViewHolder {
        View root;
        ImageView icon;
        TextView text;

        public CollectionsViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.addButton);
            text = itemView.findViewById(R.id.collectionName);
            root = itemView.getRootView();
        }
    }

    public void setCollections(ArrayList<String> collections) {
        this.collections = collections;
    }

    public interface CollectionsListener {
        void onRootClick(int position);

        void onAddClick(ImageView icon, int position);
    }
}
