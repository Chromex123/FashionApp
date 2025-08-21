package com.example.fashionapp.ui.your_posts;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.fashionapp.Post;
import com.example.fashionapp.PostAdapter;
import com.example.fashionapp.R;
import com.example.fashionapp.YourPostAdapter;
import com.example.fashionapp.databinding.FragmentYourPostsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YourPostsFragment extends Fragment implements YourPostAdapter.OnImageSelectedListener, YourPostAdapter.OnDeleteButtonListener {

    private FragmentYourPostsBinding binding;
    private RecyclerView recyclerView;
    private YourPostAdapter postAdapter;
    public static List<Post> yourPostsList;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        YourPostsViewModel yourPostsViewModel =
                new ViewModelProvider(this).get(YourPostsViewModel.class);

        binding = FragmentYourPostsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Load current posts saved in Firestore
        yourPostsList = new ArrayList<>();
        postAdapter = new YourPostAdapter(yourPostsList, this, this, requireContext().getApplicationContext());
        setUpRecycler(root);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadYourPosts(); // safe to call
        } else {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        loadYourPosts();
                        Log.i("YourPostsFragment", "Saved posts loaded");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Anonymous sign-in failed", e);
                    });
        }

        return root;
    }

    private void loadYourPosts() {
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();
        FirebaseFirestore.getInstance()
                .collection("user_gallery")
                .document(uid)
                .collection("your_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    yourPostsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference postRef = doc.getDocumentReference("postRef");

                        if(postRef != null) {
                            postRef.get().addOnSuccessListener(postDocSnap -> {
                                if(postDocSnap.exists()) {
                                    Timestamp firestoreTimestamp = postDocSnap.getTimestamp("timestamp");
                                    if(firestoreTimestamp != null) {
                                        String title = postDocSnap.getString("title");
                                        String caption = postDocSnap.getString("caption");
                                        String imageUrl = postDocSnap.getString("imageUrl");
                                        List<String> styles = (ArrayList<String>) postDocSnap.get("styles");

                                        long timestamp = firestoreTimestamp.toDate().getTime();
                                        long voteCount = Objects.requireNonNull(postDocSnap.getLong("voteCount"));
                                        long likes = Objects.requireNonNull(postDocSnap.getLong("likes"));
                                        long dislikes = Objects.requireNonNull(postDocSnap.getLong("dislikes"));
                                        Map<String, Long> votesMap = (Map<String, Long>) Objects.requireNonNull(postDocSnap.get("votesMap"));

                                        Post post = new Post(uid, title, caption, imageUrl, styles, timestamp);
                                        post.setDocId(postDocSnap.getId());
                                        post.setVoteCount(voteCount);
                                        post.setLikes(likes);
                                        post.setDislikes(dislikes);
                                        post.setVotesMap(votesMap);

                                        yourPostsList.add(post);
                                        postAdapter.notifyItemInserted(yourPostsList.size()-1);
                                    }
                                }else{
                                    FirebaseFirestore.getInstance().collection("user_gallery")
                                            .document(uid)
                                            .collection("your_posts")
                                            .document(doc.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.i("YourPostsFragment", "PostRef deleted in user's posts");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("YourPostsFragment", "Could not delete PostRef", e);
                                            });
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("YourPostsFragment", "Error loading post", e);
                                Snackbar.make(binding.getRoot(), "Error loading a post", 1000).show();
                            });
                        }
                    }
                    Log.i("YourPostsFragment","User's posts loaded");
                })
                .addOnFailureListener(e -> {
                    Log.e("YourPostsFragment", "Error loading saved posts", e);
                    Snackbar.make(binding.getRoot(), "Error loading saved posts", 1000).show();
                });
    }

    private void setUpRecycler(View root) {
        recyclerView = (RecyclerView) root.findViewById(R.id.yourPostsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(requireContext().getApplicationContext(), YourPostsActivityPostDetail.class);
        detailIntent.putExtra("position", position);
        startActivity(detailIntent);
        Log.i("YourPostsFragment", "Saved Post Detail Activity Started");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDeleteButtonClick(int position) {
        Post post = yourPostsList.get(position);
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("posts")
                            .document(post.getDocId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                Log.i("YourPostsFragment", "success");
                                // Remove from local list + update adapter
                                yourPostsList.remove(position);
                                postAdapter.notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> {
                                Log.i("YourPostsFragment", "fail");
                                //Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    dialog.dismiss(); // Just close the dialog
                })
                .show();
    }
}