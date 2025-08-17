package com.example.fashionapp.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionapp.Post;
import com.example.fashionapp.PostAdapter;
import com.example.fashionapp.R;
import com.example.fashionapp.databinding.FragmentFavoritesBinding;
import com.example.fashionapp.ui.inspiration.InspirationActivityPostDetail;
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

public class FavoritesFragment extends Fragment implements PostAdapter.OnImageSelectedListener{

    private FragmentFavoritesBinding binding;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    public static List<Post> savedPostsList;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        FavoritesViewModel favoritesViewModel =
                new ViewModelProvider(this).get(FavoritesViewModel.class);

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Load current posts saved in Firestore
        savedPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(savedPostsList, this, requireContext().getApplicationContext());
        setUpRecycler(root);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadSavedPosts(); // safe to call
        } else {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        loadSavedPosts();
                        Log.i("FavoritesFragment", "Saved posts loaded");
                    })
                    .addOnFailureListener(e -> {
                        Log.i("Firebase", "Anonymous sign-in failed", e);
                    });
        }

        return root;
    }

    private void loadSavedPosts() {
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();
        FirebaseFirestore.getInstance()
                .collection("user_gallery")
                .document(uid)
                .collection("saved_posts")
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedPostsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference postRef = doc.getDocumentReference("postRef");

                        if(postRef != null) {
                            postRef.get().addOnSuccessListener(postDocSnap -> {
                                String title = postDocSnap.getString("title");
                                String caption = postDocSnap.getString("caption");
                                String imageUrl = postDocSnap.getString("imageUrl");
                                List<String> styles = (ArrayList<String>) postDocSnap.get("styles");
                                Timestamp firestoreTimestamp = postDocSnap.getTimestamp("timestamp");
                                assert firestoreTimestamp != null;
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

                                savedPostsList.add(post);
                                postAdapter.notifyItemInserted(savedPostsList.size()-1);
                            }).addOnFailureListener(e -> {
                                Log.e("FavoritesFragment", Objects.requireNonNull(e.getMessage()));
                                Snackbar.make(binding.getRoot(), "Error loading a post", 1000).show();
                            });
                        }
                    }
                    Log.i("FavoritesFragment","User's saved posts loaded");
                })
                .addOnFailureListener(e -> {
                    Log.e("FavoritesFragment", Objects.requireNonNull(e.getMessage()));
                    Snackbar.make(binding.getRoot(), "Error loading saved posts", 1000).show();
                });
    }

    private void setUpRecycler(View root) {
        recyclerView = (RecyclerView) root.findViewById(R.id.savedPostsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(requireContext().getApplicationContext(), FavoritesActivityPostDetail.class);
        detailIntent.putExtra("position", position);
        startActivity(detailIntent);
        Log.i("FavoritesFragment", "Saved Post Detail Activity Started");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}