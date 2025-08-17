package com.example.fashionapp.ui.inspiration;

import static java.lang.Math.toIntExact;
import static java.util.Map.entry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InspirationFragment extends Fragment implements PostAdapter.OnImageSelectedListener {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    public static List<Post> postList;
    private FragmentInspirationBinding binding;
    private FirebaseAuth mAuth;
    private long mLastClickTimeFilterButton = 0;
    private final long mClickIntervalFilterButton = 1000;
    private long mLastClickTimeNewButton = 0;
    private final long mClickIntervalNewButton = 1000;
    private long mLastClickTimeTopButton = 0;
    private final long mClickIntervalTopButton = 1000;
    private final String[] orderByOptionsList = {"timestamp", "voteCount"}; // Firebase fields to order posts by
    private List<String> selectedStyles = new ArrayList<String>();
    private final String[] allTags = InspirationActivityPost.styles;
    private boolean[] selectedTags = new boolean[allTags.length];
    private String currentOrderBy = "timestamp"; //default post order

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

        LinearLayout filterFeedButton = root.findViewById(R.id.filter_feed_button);
        filterFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prevent double tapping
                if(SystemClock.elapsedRealtime() - mLastClickTimeFilterButton < mClickIntervalFilterButton) {
                    return;
                }
                mLastClickTimeFilterButton = SystemClock.elapsedRealtime();
                showTagFilterDialog();
            }
        });

        TextView newFeedText = root.findViewById(R.id.new_feed_text);
        TextView topFeedText = root.findViewById(R.id.top_feed_text);

        LinearLayout newFeedButton = root.findViewById(R.id.new_feed_button);
        newFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prevent double tapping
                if(SystemClock.elapsedRealtime() - mLastClickTimeNewButton < mClickIntervalNewButton) {
                    return;
                }
                mLastClickTimeNewButton = SystemClock.elapsedRealtime();
                currentOrderBy = "timestamp";
                loadPosts();
                newFeedText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.teal_700, null));
                topFeedText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.light_gray, null));
            }
        });

        LinearLayout topFeedButton = root.findViewById(R.id.top_feed_button);
        topFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prevent double tapping
                if(SystemClock.elapsedRealtime() - mLastClickTimeTopButton < mClickIntervalTopButton) {
                    return;
                }
                mLastClickTimeTopButton = SystemClock.elapsedRealtime();
                currentOrderBy = "voteCount";
                loadPosts();
                topFeedText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.teal_700, null));
                newFeedText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.light_gray, null));
            }
        });

        // Load current posts saved in Firestore
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this, requireContext().getApplicationContext());
        setUpRecycler(root);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadPosts(); // safe to call
        } else {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        loadPosts();
                    })
                    .addOnFailureListener(e -> {
                        Log.i("Firebase", "Anonymous sign-in failed", e);
                    });
        }

        return root;
    }

    private void loadPosts() {
        //If user has filtered posts by styles, filter post in addition to sort by orderBy
        if(!selectedStyles.isEmpty()) {
            filterPostsByTags();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy(currentOrderBy, Query.Direction.DESCENDING)
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
                    }
                    postAdapter.notifyDataSetChanged();
                    Log.i("InspirationFragment","Order By: " + currentOrderBy);
                    Log.i("InspirationFragment", "Filter: " + selectedStyles.toString());
                })
                .addOnFailureListener(e -> {
                        Log.e("InspirationFragment", Objects.requireNonNull(e.getMessage()));
                        Snackbar.make(binding.getRoot(), "Error loading posts", 1000).show();
                });
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

    private void showTagFilterDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Select Styles")
                .setMultiChoiceItems(allTags, selectedTags, (dialog, which, isChecked) -> {
                    selectedTags[which] = isChecked;
                })
                .setPositiveButton("Apply", (dialog, which) -> {
                    selectedStyles.clear();
                    for (int i = 0; i < allTags.length; i++) {
                        if (selectedTags[i]) {
                            selectedStyles.add(allTags[i]);
                        }
                    }
                    filterPostsByTags();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterPostsByTags() {
        if (selectedStyles.isEmpty()) {
            // Load all posts if no tags selected
            TextView stylesText = binding.getRoot().findViewById(R.id.filter_feed_text);
            stylesText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.light_gray, null));
            loadPosts();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("posts")
                .whereArrayContainsAny("styles", selectedStyles)
                .orderBy(currentOrderBy, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        // Check if styles has all selectedStyles before continuing with post creation
                        List<String> styles = (ArrayList<String>) doc.get("styles");
                        assert styles != null;
                        if (new HashSet<>(styles).containsAll(selectedStyles)) {
                            String uid = doc.getString("uid");
                            String title = doc.getString("title");
                            String caption = doc.getString("caption");
                            String imageUrl = doc.getString("imageUrl");
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
                            post.setDocId(doc.getId());

                            postList.add(post);
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                    TextView stylesText = binding.getRoot().findViewById(R.id.filter_feed_text);
                    stylesText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.teal_700, null));
                    Log.i("InspirationFragment","Order By: " + currentOrderBy);
                    Log.i("InspirationFragment", "Filter: " + selectedStyles.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("InspirationFragment", Objects.requireNonNull(e.getMessage()));
                    Snackbar.make(binding.getRoot(), "Error loading posts", 1000).show();
                });
    }
}