package com.example.fashionapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecycleAdapter binds images to the RecyclerView in main app screen, displaying the user's
 * uploaded images in a neat grid view.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    private Context context;
    private List<String> recyclingArrayList;
    private OnItemListener onItemListener;
    private OnItemLongClickListener longClickListener;
    private Set<Integer> selectedPositions = new HashSet<>();
    private boolean selectionMode = false;
    private OnSelectionChangedListener selectionChangedListener;

    public RecycleAdapter(Context context, List<String> recyclingArrayList, OnItemListener onItemListener, OnItemLongClickListener longClickListener) {
        this.recyclingArrayList = recyclingArrayList;
        this.onItemListener = onItemListener;
        this.longClickListener = longClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_cell, parent, false);
        return new ViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(context)
            .load(recyclingArrayList.get(holder.getBindingAdapterPosition()))
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                            Target<Drawable> target, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return false; // Let Glide handle error display
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model,
                                               Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return false; // Let Glide handle displaying the image
                }
            })
            .into(holder.recycleImage);

        boolean selected = selectedPositions.contains(holder.getBindingAdapterPosition());
        holder.overlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        holder.checkIcon.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            selectionMode = true;
            selectedPositions.add(holder.getBindingAdapterPosition());
            notifyItemChanged(holder.getBindingAdapterPosition());
            if (longClickListener != null) {
                longClickListener.onItemLongClick();
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                if (selectedPositions.contains(holder.getBindingAdapterPosition())) {
                    selectedPositions.remove(holder.getBindingAdapterPosition());
                } else {
                    selectedPositions.add(holder.getBindingAdapterPosition());
                }
                notifyItemChanged(holder.getBindingAdapterPosition());

                if (selectionChangedListener != null) {
                    selectionChangedListener.onSelectionChanged(!selectedPositions.isEmpty(), selectedPositions.size());
                }

                if (selectedPositions.isEmpty()) {
                    selectionMode = false;
                    notifyDataSetChanged();
                }
            } else {
                // Normal click behavior
                if (onItemListener != null) {
                    onItemListener.onItemClick(holder.getBindingAdapterPosition());
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

    public List<String> getSelectedUris() {
        List<String> selectedUris = new ArrayList<>();
        for (int pos : selectedPositions) {
            selectedUris.add(recyclingArrayList.get(pos));
        }
        return selectedUris;
    }

    public void setImageUris(List<String> newUris) {
        this.recyclingArrayList = newUris;
        this.selectedPositions.clear();
        this.selectionMode = false;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPositions.clear();
        selectionMode = false;
        this.notifyDataSetChanged();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void clear() {
        int size = recyclingArrayList.size();
        recyclingArrayList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView recycleImage;
        private OnItemListener onItemListener;
        View overlay;
        ImageView checkIcon;
        ProgressBar progressBar;

        public ViewHolder(View itemView, OnItemListener onItemListener) {
            super(itemView);
            recycleImage = (ImageView) itemView.findViewById(R.id.photo_1);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
            overlay = itemView.findViewById(R.id.selection_overlay);
            checkIcon = itemView.findViewById(R.id.check_icon);
            progressBar = itemView.findViewById(R.id.image_progress);
        }

        @Override
        public void onClick(View view) {
            //Log.i("HomeFragment","Item click");
            onItemListener.onItemClick(getBindingAdapterPosition());
        }
    }
}
