package com.example.fashionapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnOutfitSelectedListener listener;

    public interface OnOutfitSelectedListener {
        void onItemClick(int position);
    }

    public OutfitAdapter(List<String> imageUrls, OnOutfitSelectedListener listener, Context context) {
        this.imageUrls = imageUrls;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_cell, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Glide.with(context)
                .load(imageUrls.get(holder.getBindingAdapterPosition()))
                .centerCrop()
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

        boolean isSelected = selectedPosition == holder.getBindingAdapterPosition();
        holder.overlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            if (selectedPosition == holder.getBindingAdapterPosition()) {
                selectedPosition = RecyclerView.NO_POSITION;
            } else {
                selectedPosition = holder.getBindingAdapterPosition();
            }
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            if(listener != null) {
                listener.onItemClick(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public String getSelectedUri() {
        return imageUrls.get(selectedPosition);
    }

    public class OutfitViewHolder extends RecyclerView.ViewHolder {
        private ImageView recycleImage;
        View overlay;
        ImageView checkIcon;
        ProgressBar progressBar;

        public OutfitViewHolder(View itemView) {
            super(itemView);
            recycleImage = (ImageView) itemView.findViewById(R.id.photo_1);
            overlay = itemView.findViewById(R.id.selection_overlay);
            checkIcon = itemView.findViewById(R.id.check_icon);
            progressBar = itemView.findViewById(R.id.image_progress);
        }
    }
}
