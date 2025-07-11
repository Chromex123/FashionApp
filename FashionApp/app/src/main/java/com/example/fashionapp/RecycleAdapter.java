package com.example.fashionapp;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {
    private List<Uri> recyclingArrayList;
    private OnItemListener onItemListener;
    private OnItemLongClickListener longClickListener;
    private Set<Integer> selectedPositions = new HashSet<>();
    private boolean selectionMode = false;
    private OnSelectionChangedListener selectionChangedListener;


    public RecycleAdapter(List<Uri> recyclingArrayList, OnItemListener onItemListener, OnItemLongClickListener longClickListener) {
        this.recyclingArrayList = recyclingArrayList;
        this.onItemListener = onItemListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_cell, parent, false);
        return new ViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.recycleImage.setImageURI(recyclingArrayList.get(position));

        boolean selected = selectedPositions.contains(position);
        holder.overlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        holder.checkIcon.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            selectionMode = true;
            selectedPositions.add(position);
            notifyItemChanged(position);
            if (longClickListener != null) {
                longClickListener.onItemLongClick();
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                } else {
                    selectedPositions.add(position);
                }
                notifyItemChanged(position);

                if (selectionChangedListener != null) {
                    selectionChangedListener.onSelectionChanged(!selectedPositions.isEmpty(), selectedPositions.size());
                }

                if (selectedPositions.isEmpty()) {
                    selectionMode = false;
                    notifyDataSetChanged();
                }
            } else {
                // Normal click behavior (e.g., open image fullscreen)
                if (onItemListener != null) {
                    onItemListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclingArrayList.size();
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean isInSelectionMode, int selectedCount);
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public List<Uri> getSelectedUris() {
        List<Uri> selectedUris = new ArrayList<>();
        for (int pos : selectedPositions) {
            selectedUris.add(recyclingArrayList.get(pos));
        }
        return selectedUris;
    }

    public void setImageUris(List<Uri> newUris) {
        this.recyclingArrayList = newUris;
        this.selectedPositions.clear();
        this.selectionMode = false;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPositions.clear();
        selectionMode = false;
        this.notifyDataSetChanged(); //remove 'this' if error
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView recycleImage;
        private OnItemListener onItemListener;
        View overlay;
        ImageView checkIcon;

        public ViewHolder(View itemView, OnItemListener onItemListener) {
            super(itemView);
            recycleImage = (ImageView) itemView.findViewById(R.id.photo_1);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
            overlay = itemView.findViewById(R.id.selection_overlay);
            checkIcon = itemView.findViewById(R.id.check_icon);
        }

        @Override
        public void onClick(View view) {
            Log.i("HomeFragment","Item click");
            onItemListener.onItemClick(getBindingAdapterPosition());
        }
    }
}
