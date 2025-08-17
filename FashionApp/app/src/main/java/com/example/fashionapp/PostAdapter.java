package com.example.fashionapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    Context context;
    private PostAdapter.OnImageSelectedListener listener;

    public interface OnImageSelectedListener {
        void onItemClick(int position);
    }

    public PostAdapter(List<Post> postList, OnImageSelectedListener listener, Context context) {
        this.postList = postList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_cell, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(holder.getBindingAdapterPosition());
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();
        Long userVote = (post.getVotesMap() != null) ? post.getVotesMap().get(uid) : null;
        holder.title.setText(post.getTitle());
        holder.caption.setText(post.getCaption());

        // Convert timestamp to "time ago"
        long diff = System.currentTimeMillis() - post.getTimestamp();
        holder.time.setText(getTimeAgo(diff));

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.inspiration_default_image) // default image
                .centerCrop()
                .into(holder.image);

        holder.image.setOnClickListener(v -> {
            if(listener != null) {
                listener.onItemClick(holder.getBindingAdapterPosition());
            }
        });

        //Glide.with(context).load(post.getImageUrl()).into(holder.image);

        // Set the vote count of the post
        holder.voteCount.setText(String.valueOf(post.getVoteCount()));

        // Highlight the text/buttons based on the vote
        if(userVote == null) {
            holder.voteCount.setTextColor(Color.WHITE);
        }
        else if(userVote == 1) {
            holder.voteCount.setTextColor(context.getResources().getColor(R.color.teal_700, null));
        }else{
            holder.voteCount.setTextColor(Color.RED);
        }

        holder.upvoteButton.setColorFilter(
                (userVote != null && userVote == 1) ? context.getResources().getColor(R.color.teal_700, null) : Color.WHITE
        );
        holder.downvoteButton.setColorFilter(
                (userVote != null && userVote == -1) ? Color.RED : Color.WHITE
        );

        FirebaseFirestore.getInstance().collection("user_gallery")
                .document(uid)
                .collection("saved_posts")
                .document(post.getDocId())
                .get()
                .addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        holder.saveButton.setColorFilter(Color.YELLOW);;
                    } else {
                        holder.saveButton.setColorFilter(Color.WHITE);
                    }
                })
                .addOnFailureListener(e -> {
                    //Snackbar.make(holder.itemView, "Failed to unsave post", 1000).show();
                    Log.e("InspirationFragment","Failed to retrieve post", e);
                });

        holder.upvoteButton.setOnClickListener(v -> {
            vote(post, 1, holder);

            //int newLikes = post.getLikes() + 1;
            //int newDislikes = post.getDislikes() - 1;

            //post.setLikes(newLikes);
            //post.setDislikes(newDislikes);
        });

        holder.downvoteButton.setOnClickListener(v -> {
            vote(post, -1, holder);

            //int newDislikes = post.getDislikes() + 1;
            //int newLikes = post.getLikes() - 1;

            //post.setDislikes(newDislikes);
            //post.setLikes(newLikes);
        });

        // Saving a post
        holder.saveButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference postRef = db.collection("user_gallery")
                    .document(uid)
                    .collection("saved_posts")
                    .document(post.getDocId());

            postRef.get().addOnSuccessListener(docSnapshot -> {
                if (docSnapshot.exists()) {
                    //If Post is already saved then unsave it
                    postRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                //Snackbar.make(holder.itemView, "Post unsaved", 1000).show();
                                Log.i("InspirationFragment","Post unsaved");
                                holder.saveButton.setColorFilter(Color.WHITE);
                            })
                            .addOnFailureListener(e -> {
                                //Snackbar.make(holder.itemView, "Failed to unsave post", 1000).show();
                                Log.e("InspirationFragment","Failed to unsave post", e);
                            });
                } else {
                    //If Post not saved then save it
                    Map<String, Object> savedData = new HashMap<>();
                    savedData.put("postRef", db.collection("posts").document(post.getDocId()));
                    savedData.put("savedAt", FieldValue.serverTimestamp());

                    postRef.set(savedData)
                            .addOnSuccessListener(aVoid -> {
                                Log.i("InspirationFragment","Post saved success");
                                holder.saveButton.setColorFilter(Color.YELLOW);
                            })
                            .addOnFailureListener(e -> {
                                //Snackbar.make(holder.itemView, "Failed to save post", 1000).show();
                                Log.e("InspirationFragment","Failed to save post", e);
                            });
                }
            });
        });

        // Showing the styles
        holder.styles.removeAllViews();
        if (post.getStyles() != null) {
            for (String tag : post.getStyles()) {
                TextView tagView = new TextView(context);
                tagView.setText(tag);
                tagView.setTextSize(12);
                tagView.setPadding(24, 12, 24, 12);
                tagView.setBackgroundResource(R.drawable.bg_inspiration_styles);
                tagView.setTextColor(ContextCompat.getColorStateList(context, R.color.white));

                // Optional: margin
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 8, 8, 8);
                tagView.setLayoutParams(params);

                // Make selectable
                tagView.setClickable(false);

                holder.styles.addView(tagView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private String getTimeAgo(long diffMillis) {
        long seconds = diffMillis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ";
        long days = hours / 24;
        if (days < 365) return days + "d";
        long years = days / 365;
        return years + "y";
    }

    public void clear() {
        int size = postList.size();
        postList.clear();
        notifyItemRangeRemoved(0, size);
    }

    private void vote(Post post, int newVoteValue, PostViewHolder holder) {
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();
        DocumentReference postDoc = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.getDocId());

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(postDoc);

            // Get current votes map (or empty if none)
            Map<String, Long> votesMap = (Map<String, Long>) snapshot.get("votesMap");
            if (votesMap == null) {
                votesMap = new HashMap<>();
            }

            Long currentVoteValue = votesMap.get(uid);
            long voteCount = snapshot.getLong("voteCount") != null ? snapshot.getLong("voteCount") : 0;

            if (currentVoteValue != null) {
                // User already voted
                if (currentVoteValue == newVoteValue) {
                    // Remove the vote
                    votesMap.remove(uid);
                    post.setVotesMap(votesMap);
                    voteCount -= newVoteValue;
                    post.setVoteCount(voteCount);
                } else {
                    // Change the vote
                    votesMap.put(uid, (long) newVoteValue);
                    post.setVotesMap(votesMap);
                    voteCount += (newVoteValue - currentVoteValue);
                    post.setVoteCount(voteCount);
                }
            } else {
                // No previous vote → add it
                votesMap.put(uid, (long) newVoteValue);
                post.setVotesMap(votesMap);
                voteCount += newVoteValue;
                post.setVoteCount(voteCount);
            }

            transaction.update(postDoc, "votesMap", votesMap, "voteCount", voteCount);

            return null;
        }).addOnSuccessListener(aVoid -> {
            notifyItemChanged(holder.getBindingAdapterPosition());
            Log.i("InspirationFragment", "Vote updated successfully");
        }).addOnFailureListener(e -> {
            Log.e("InspirationFragment", "Failed to update vote", e);
        });
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, caption, time, voteCount;
        FlexboxLayout styles;
        ImageButton upvoteButton, downvoteButton, saveButton;

        public PostViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            caption = itemView.findViewById(R.id.postCaption);
            image = itemView.findViewById(R.id.postImage);
            styles = itemView.findViewById(R.id.postStyles);
            time = itemView.findViewById(R.id.postTime);
            voteCount = itemView.findViewById(R.id.voteCount);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            downvoteButton = itemView.findViewById(R.id.downvoteButton);
            saveButton = itemView.findViewById(R.id.saveButton);
        }
    }
}
