package com.harrydmorgan.shoppinglist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ExpandableSection extends Section {

    private final String title;
    private final ArrayList<String> items;


    private final ClickListener clickListener;

    private boolean expanded = true;

    ExpandableSection(@NonNull final String title, @NonNull final ArrayList<String> items,
                              @NonNull final ClickListener clickListener) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.list_item)
                .headerResourceId(R.layout.list_header)
                .build());

        this.title = title;
        this.items = items;
        this.clickListener = clickListener;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getContentItemsTotal() {
        return expanded ? items.size() : 0;
    }


    //Item adapter
    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemView = (ItemViewHolder) holder;

        itemView.itemTxt.setText(items.get(position));
        itemView.root.setOnClickListener(v -> clickListener.onItemRootViewClicked(this, itemView.getAdapterPosition()));
    }


    //Header adapter
    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder header = (HeaderViewHolder) holder;

        header.titleTxt.setText(title);
        header.root.setOnClickListener(v -> clickListener.onHeaderRootViewClicked(this));

    }

    boolean isExpanded() {
        return expanded;
    }

    void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    //Class for item view holder
    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView itemTxt;
        View root;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTxt = itemView.findViewById(R.id.listText);
            root = itemView.getRootView();
        }
    }

    //Class for header view holder
    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        TextView titleTxt;
        View root;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            titleTxt = itemView.findViewById(R.id.header_title);
        }
    }


    interface ClickListener {

        void onHeaderRootViewClicked(@NonNull final ExpandableSection section);

        void onItemRootViewClicked(@NonNull final ExpandableSection section, final int itemAdapterPosition);
    }
}
