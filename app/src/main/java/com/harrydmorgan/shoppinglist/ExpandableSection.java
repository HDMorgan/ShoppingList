package com.harrydmorgan.shoppinglist;

import android.content.ClipData;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ExpandableSection extends Section {

    private final String title;
    private final ArrayList<String> items;
    private String type;
    private ArrayList<String> checkedCategories;

    private final ClickListener clickListener;

    private boolean expanded = true;

    ExpandableSection(@NonNull final String title, @NonNull final ArrayList<String> items,
                              @NonNull final ClickListener clickListener, String type) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.list_item)
                .headerResourceId(R.layout.list_header)
                .build());
        this.type = type;
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
        if (type.equals("Checked")) {
            return new CheckedItemViewHolder(view);
        }
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemView = (ItemViewHolder) holder;

        if (type == null) {
            itemView.divider.setVisibility(View.GONE);
            itemView.icon.setVisibility(View.GONE);
        } else if (type.equals("Checked")) {
            itemView.icon.setImageResource(R.drawable.ic_angled_arrow);
            itemView.icon.setRotation(-90);
            itemView.category.setText(checkedCategories.get(position));
            itemView.category.setVisibility(View.VISIBLE);
        }

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
        header.arrow.setImageResource(
                expanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down
        );
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
        ImageView icon;
        View divider;
        TextView category;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTxt = itemView.findViewById(R.id.listText);
            category = itemView.findViewById(R.id.list_group);
            root = itemView.getRootView();
            icon = itemView.findViewById(R.id.list_icon);
            divider = itemView.findViewById(R.id.list_divider);
        }
    }

    public class CheckedItemViewHolder extends ItemViewHolder {
        CheckedItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    //Class for header view holder
    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        TextView titleTxt;
        View root;
        ImageView arrow;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            titleTxt = itemView.findViewById(R.id.header_title);
            arrow = itemView.findViewById(R.id.list_arrow);
        }
    }

    public void setCheckedCategories(ArrayList<String> checkedCategories) {
        this.checkedCategories = checkedCategories;
    }

    public String getItem(int position) {return items.get(position);}

    public void removeItem(int position) {
        items.remove(position);
        if (type.equals("Checked")) {
            checkedCategories.remove(position);
        }
    }

    public String getCheckedCategory(int position) {return checkedCategories.get(position);}

    interface ClickListener {

        void onHeaderRootViewClicked(@NonNull final ExpandableSection section);

        void onItemRootViewClicked(@NonNull final ExpandableSection section, final int itemAdapterPosition);
    }
}
