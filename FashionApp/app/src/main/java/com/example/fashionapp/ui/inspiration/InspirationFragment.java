package com.example.fashionapp.ui.inspiration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fashionapp.Post;
import com.example.fashionapp.PostAdapter;
import com.example.fashionapp.R;
import com.example.fashionapp.databinding.FragmentInspirationBinding;
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

/**
 * InspirationFragment displays a feed of posts from all users, allowing
 * filtering by tags, sorting by newest or most upvotes, searching by title, and refreshing to load new content.
 */
public class InspirationFragment extends Fragment implements PostAdapter.OnImageSelectedListener {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private PostAdapter queryPostAdapter;
    public static List<Post> postList;
    private List<Post> queryPostList;
    private boolean isQueryActive = false;
    private StringBuilder queryText = new StringBuilder();
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
    private boolean[] selectedTags = new boolean[allTags.length]; // For filter by styles alert dialog
    private String currentOrderBy = "timestamp"; //default post order

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentInspirationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        super.onViewCreated(root, savedInstanceState);

        //Set up default post adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this, requireContext().getApplicationContext());
        setUpRecycler(root);

        //Initialize post adapter to use when user queries for posts
        queryPostList = new ArrayList<>();
        queryPostAdapter = new PostAdapter(queryPostList, this, requireContext().getApplicationContext());

        setupKeyboardDismissOnTouch(root);

        // Set up refresh listener for posts
        SwipeRefreshLayout swipeRefreshPosts = root.findViewById(R.id.swipeRefreshPosts);
        RecyclerView recyclerView = root.findViewById(R.id.feedRecyclerView);

        // Reload posts
        swipeRefreshPosts.setOnRefreshListener(() -> {
            // Reload your posts here
            loadPosts();
            //Log.i("InspirationFragment","Posts reloaded");
        });

        ImageButton newPostButton = root.findViewById(R.id.newPostButton);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewPostButtonClick(v, root);
            }
        });

        SearchView searchView = root.findViewById(R.id.search_bar);
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView searchTextView = (TextView) searchView.findViewById(id);
        searchTextView.setHintTextColor(Color.WHITE);
        searchTextView.setTextColor(Color.WHITE);

        id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView searchClose = searchView.findViewById(id);
        searchClose.setColorFilter(requireContext().getApplicationContext().getResources().getColor(R.color.light_gray, null));

        id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchImageView = (ImageView) searchView.findViewById(id);
        searchImageView.setColorFilter(Color.WHITE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                hideKeyboard(requireActivity().getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()) {
                    recyclerView.setAdapter(postAdapter);
                    isQueryActive = false;
                    //Log.i("InspirationFragment","Adapter using posts list");
                }
                return true;
            }

            private void filterPosts(String text) {
                queryPostList.clear();
                for (Post post : postList) { // allPostsList = full list from Firestore
                    if (post.getTitle().toLowerCase().contains(text.trim().toLowerCase())) {
                        queryPostList.add(post);
                    }
                }
                recyclerView.setAdapter(queryPostAdapter);
                queryPostAdapter.notifyDataSetChanged();

                queryText.setLength(0);
                queryText.append(text);
                isQueryActive = true;

                //Log.i("InspirationFragment","Adapter using queried list, queried for: " + queryText.toString());
            }
        });

        searchView.setOnCloseListener(() -> {
            searchView.setQuery("", false);
            //searchView.setIconified(true);
            recyclerView.setAdapter(postAdapter);
            isQueryActive = false;
            //Log.i("InspirationFragment","Adapter using posts list");
            return false;
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadPosts(); // safe to call
        } else {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        loadPosts();
                    })
                    .addOnFailureListener(e -> {
                        //Log.e("Firebase", "Anonymous sign-in failed", e);
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

        SwipeRefreshLayout swipeRefreshPosts = binding.getRoot().findViewById(R.id.swipeRefreshPosts);

        FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy(currentOrderBy, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp firestoreTimestamp = doc.getTimestamp("timestamp");
                        if(firestoreTimestamp != null) {
                            String uid = doc.getString("uid");
                            String title = doc.getString("title");
                            String caption = doc.getString("caption");
                            String imageUrl = doc.getString("imageUrl");
                            List<String> styles = (ArrayList<String>) doc.get("styles");

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
                    }
                    if(isQueryActive) {
                        queryPostList.clear();
                        for (Post post : postList) { // allPostsList = full list from Firestore
                            if (post.getTitle().toLowerCase().contains(queryText.toString().trim().toLowerCase())) {
                                queryPostList.add(post);
                            }
                        }
                        recyclerView.setAdapter(queryPostAdapter);
                        queryPostAdapter.notifyDataSetChanged();
                    }else{
                        recyclerView.setAdapter(postAdapter);
                        postAdapter.notifyDataSetChanged();
                    }
                    swipeRefreshPosts.setRefreshing(false);
                    //Log.i("InspirationFragment","Order By: " + currentOrderBy);
                    //Log.i("InspirationFragment", "Filter: " + selectedStyles.toString());
                })
                .addOnFailureListener(e -> {
                    swipeRefreshPosts.setRefreshing(false);
                    //Log.e("InspirationFragment", Objects.requireNonNull(e.getMessage()));
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
        //Log.i("InspirationFragment","Button click");
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
        //Log.i("InspirationFragment", "Post Select Activity Started");
    }

    /**
     * Shows the alert dialog pop-up that allows the user to select multiple styles to filter posts by.
     */
    private void showTagFilterDialog() {
        new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogTheme)
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

    /**
     * If no styles are selected, load the unfiltered post feed, otherwise filter the feed.
     */
    private void filterPostsByTags() {
        if (selectedStyles.isEmpty()) {
            // Load all posts if no tags selected
            TextView stylesText = binding.getRoot().findViewById(R.id.filter_feed_text);
            stylesText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.light_gray, null));
            loadPosts();
            return;
        }

        SwipeRefreshLayout swipeRefreshPosts = binding.getRoot().findViewById(R.id.swipeRefreshPosts);
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
                        if(styles != null) {
                            if (new HashSet<>(styles).containsAll(selectedStyles)) {
                                Timestamp firestoreTimestamp = doc.getTimestamp("timestamp");
                                if(firestoreTimestamp != null) {
                                    String uid = doc.getString("uid");
                                    String title = doc.getString("title");
                                    String caption = doc.getString("caption");
                                    String imageUrl = doc.getString("imageUrl");

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
                        }
                    }
                    if(isQueryActive) {
                        queryPostList.clear();
                        for (Post post : postList) { // allPostsList = full list from Firestore
                            if (post.getTitle().toLowerCase().contains(queryText.toString().trim().toLowerCase())) {
                                queryPostList.add(post);
                            }
                        }
                        recyclerView.setAdapter(queryPostAdapter);
                        queryPostAdapter.notifyDataSetChanged();
                    }else{
                        recyclerView.setAdapter(postAdapter);
                        postAdapter.notifyDataSetChanged();
                    }
                    swipeRefreshPosts.setRefreshing(false);

                    TextView stylesText = binding.getRoot().findViewById(R.id.filter_feed_text);
                    stylesText.setTextColor(requireContext().getApplicationContext().getResources().getColor(R.color.teal_700, null));
                    //Log.i("InspirationFragment","Order By: " + currentOrderBy);
                    //Log.i("InspirationFragment", "Filter: " + selectedStyles.toString());
                })
                .addOnFailureListener(e -> {
                    swipeRefreshPosts.setRefreshing(false);

                    //Log.e("InspirationFragment", Objects.requireNonNull(e.getMessage()));
                    Snackbar.make(binding.getRoot(), "Error loading posts", 1000).show();
                });
    }
}