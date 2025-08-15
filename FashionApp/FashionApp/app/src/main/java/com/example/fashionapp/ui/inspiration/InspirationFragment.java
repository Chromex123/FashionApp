package com.example.fashionapp.ui.inspiration;

import static java.lang.Math.toIntExact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionapp.Post;
import com.example.fashionapp.PostAdapter;
import com.example.fashionapp.R;
import com.example.fashionapp.RecycleAdapter;
import com.example.fashionapp.databinding.FragmentInspirationBinding;
import com.example.fashionapp.ui.home.HomeActivityDetail;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InspirationFragment extends Fragment implements PostAdapter.OnImageSelectedListener {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    public static List<Post> postList;
    private FragmentInspirationBinding binding;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        InspirationViewModel inspirationViewModel =
                new ViewModelProvider(this).get(InspirationViewModel.class);

        binding = FragmentInspirationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        super.onViewCreated(root, savedInstanceState);

        setupKeyboardDismissOnTouch(root);

        ImageButton newPostButton = root.findViewById(R.id.newPostButton);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewPostButtonClick(v, root);
            }
        });

        // Load current posts saved in Firestore
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this, requireContext().getApplicationContext());
        setUpRecycler(root);

        loadPosts();

        return root;
    }

    private void loadPosts() {
        FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getString("uid");
                        String title = doc.getString("title");
                        String caption = doc.getString("caption");
                        String imageUrl = doc.getString("imageUrl");
                        List<String> styles = (ArrayList<String>) doc.get("styles");
                        Timestamp firestoreTimestamp = doc.getTimestamp("timestamp");
                        assert firestoreTimestamp != null;
                        long timestamp = firestoreTimestamp.toDate().getTime();
                        long voteCount = Objects.requireNonNull(doc.getLong("voteCount"));
                        long likes = Objects.requireNonNull(doc.getLong("likes"));
                        long dislikes = Objects.requireNonNull(doc.getLong("dislikes"));
                        Map<String, Long> votesMap = (Map<String, Long>) Objects.requireNonNull(doc.get("votesMap"));

                        Post post = new Post(uid, title, caption, imageUrl, styles, timestamp);
                        post.setDocId(doc.getId());
                        post.setVoteCount(voteCount);
                        post.setLikes(likes);
                        post.setDislikes(dislikes);
                        post.setVotesMap(votesMap);

                        postList.add(post);
                        Log.i("InspirationFragment","Post loaded");
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(binding.getRoot(), "Error loading posts: " + e.getMessage(), 1000).show()
                );
    }

    private void setUpRecycler(View root) {
        recyclerView = (RecyclerView) root.findViewById(R.id.feedRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(postAdapter);
    }

    private void onNewPostButtonClick(View clicked, View view) {
        Log.i("InspirationFragment","Button click");
        ((ImageButton) clicked).setEnabled(false);
        Intent newPostIntent = new Intent(requireContext().getApplicationContext(), InspirationActivityPost.class);
        startActivity(newPostIntent);
    }
    @Override
    public void onResume() {
        super.onResume();
        ImageButton newPostButton = binding.getRoot().findViewById(R.id.newPostButton);
        newPostButton.setEnabled(true);
    }

    private void setupKeyboardDismissOnTouch(View rootView) {
        if (!(rootView instanceof EditText)) {
            rootView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    View focusedView = requireActivity().getCurrentFocus();
                    if (focusedView instanceof EditText) {
                        focusedView.clearFocus();
                        hideKeyboard(focusedView);
                    }
                }
                return false;
            });
        }

        // Recursively apply to children
        if (rootView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
                View child = ((ViewGroup) rootView).getChildAt(i);
                setupKeyboardDismissOnTouch(child);
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        postAdapter.clear();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(requireContext().getApplicationContext(), InspirationActivityPostDetail.class);
        detailIntent.putExtra("position", position);
        startActivity(detailIntent);
        Log.i("InspirationFragment", "Post Select Activity Started");
    }
}