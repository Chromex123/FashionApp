package com.example.fashionapp.ui.inspiration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fashionapp.Post;
import com.example.fashionapp.R;
import com.example.fashionapp.RecycleAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InspirationActivityPost extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String image = "";
    private Post newPost;
    private long mLastClickTimePostButton = 0;
    private final long mClickIntervalPostButton = 3000;
    private long mLastClickTimeImageView = 0;
    private final long mClickIntervalImageView = 1000;
    private boolean outfitSelected = false;
    public static final String[] styles = {"Men", "Women", "Casual", "Formal", "Streetwear", "Baggy",
                                     "Sporty", "Vintage", "Chic", "Retro", "Old Money", "Business Casual"};
    private final int maxTitleNewlines = 4;
    private final int maxCaptionNewlines = 25;

    private ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String imageUrl = data.getStringExtra("selectedImageUrl");
                                if(imageUrl == null || imageUrl.isEmpty()) {
                                    Glide.with(this).load("")
                                            .centerCrop()
                                            .into((ImageView) findViewById(R.id.selectedImageView));
                                    outfitSelected = false;
                                    image = "";
                                }else{
                                    // Use Glide or whatever you want
                                    Glide.with(this).load(imageUrl)
                                            .centerCrop()
                                            .into((ImageView) findViewById(R.id.selectedImageView));
                                    outfitSelected = true;
                                    image = imageUrl;
                                }
                            }
                        }
                        Button selectOutfitButton = findViewById(R.id.selectOutfitButton);
                        selectOutfitButton.setEnabled(true);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_inspiration_post);
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); */

        mAuth = FirebaseAuth.getInstance();

        setupKeyboardDismissOnTouch(findViewById(android.R.id.content).getRootView());

        // Handling onClick Buttons

        Button selectOutfitButton = findViewById(R.id.selectOutfitButton);
        selectOutfitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectOutfitButtonClick(v);
            }
        });

        ImageView selectedImageView = findViewById(R.id.selectedImageView);
        selectedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedImageViewClick(v);
            }
        });

        ChipGroup chipGroup = findViewById(R.id.tagChipGroup);
        for (String tag : styles) {
            Chip chip = new Chip(this);

            chip.setText(tag);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true);

            chipGroup.addView(chip);
        }

        //Listen for selection changes
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedTags = new ArrayList<>();
            for (int id : checkedIds) {
                Chip selectedChip = group.findViewById(id);
                selectedTags.add(selectedChip.getText().toString());
            }
            //Log.i("InspirationActivityPost", "Current styles: " + selectedTags);
        });

        Button submitPostButton = findViewById(R.id.submitPostButton);
        submitPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitPostButtonClick(v);
            }
        });
    }

    private void onSelectOutfitButtonClick(View clicked) {
        Log.i("InspirationActivityPost","Button click");
        ((Button) clicked).setEnabled(false);
        Intent intent = new Intent(getApplicationContext(), InspirationActivityOutfitSelect.class);
        launcher.launch(intent);
    }

    private void onSelectedImageViewClick(View clicked) {
        Log.i("InspirationActivityPost","ImageView click");
        //Prevent double tapping
        if(SystemClock.elapsedRealtime() - mLastClickTimeImageView < mClickIntervalImageView) {
            return;
        }
        mLastClickTimeImageView = SystemClock.elapsedRealtime();

        Intent intent = new Intent(getApplicationContext(), InspirationActivityOutfitDetail.class);
        intent.putExtra("imageUrl", image);
        startActivity(intent);
    }

    private void onSubmitPostButtonClick(View clicked) {
        boolean validPost = true;

        //Prevent double tapping
        if(SystemClock.elapsedRealtime() - mLastClickTimePostButton < mClickIntervalPostButton) {
            return;
        }
        mLastClickTimePostButton = SystemClock.elapsedRealtime();

        //Get post title
        EditText titleEditText = findViewById(R.id.postTitle);
        Editable titleEditable = titleEditText.getText();
        String title = titleEditable.toString().trim();

        //Ensure a image is selected and post title is not blank
        if(title.isEmpty() || !outfitSelected) {
            validPost = false;
            Snackbar.make(findViewById(android.R.id.content).getRootView(),
                    "Title blank or no image selected.", 3000)
                    .setAnchorView(findViewById(R.id.snackbarAnchor)).
                    show();
        }else{
            //Get post caption
            EditText captionEditText = findViewById(R.id.postCaption);
            Editable captionEditable = captionEditText.getText();
            String caption = captionEditable.toString().trim();

            // Get all selected tags
            ChipGroup chipGroup = findViewById(R.id.tagChipGroup);
            List<Integer> selectedIds = chipGroup.getCheckedChipIds();
            List<String> selectedTags = new ArrayList<>();

            for (int id : selectedIds) {
                Chip chip = chipGroup.findViewById(id);
                selectedTags.add(chip.getText().toString());
            }

            //Check for excessive newline characters in title and caption
            char newline = '\n';
            int countTitle = 0;
            int countCaption = 0;

            for (int i = 0; i < title.length(); i++) {
                if (title.charAt(i) == newline) {
                    countTitle++;
                }
            }
            for (int i = 0; i < caption.length(); i++) {
                if (caption.charAt(i) == newline) {
                    countCaption++;
                }
            }

            if(countTitle >= maxTitleNewlines || countCaption >= maxCaptionNewlines) {
                validPost = false;
                Snackbar.make(findViewById(android.R.id.content).getRootView(),
                        "Too many newlines in title or caption.", 3000).show();
            }

            //Create new post if valid
            if(validPost) {
                //Change here
                String uid = (Objects.requireNonNull(mAuth.getCurrentUser())).getUid();
                newPost = new Post(uid, title, caption, image, selectedTags,0);
                Log.i("InspirationActivityPost", "PostInfo: " + newPost);

                Map<String, Object> post = new HashMap<>();
                post.put("uid", newPost.getUid());
                post.put("title", newPost.getTitle());
                post.put("caption", newPost.getCaption());
                post.put("imageUrl", newPost.getImageUrl());
                post.put("styles", newPost.getStyles());
                FieldValue timestamp = FieldValue.serverTimestamp();
                post.put("timestamp", timestamp); // Firestore server time
                post.put("voteCount", newPost.getVoteCount());
                post.put("votesMap", new HashMap<String, Long>());
                post.put("likes", newPost.getLikes());
                post.put("dislikes", newPost.getDislikes());

                FirebaseFirestore.getInstance()
                        .collection("posts")
                        .add(post)
                        .addOnSuccessListener(dRef -> {
                            //Add to user's posts
                            Map<String, Object> savedData = new HashMap<>();
                            savedData.put("postRef", FirebaseFirestore.getInstance().collection("posts").document(dRef.getId()));
                            savedData.put("createdAt", timestamp);

                            FirebaseFirestore.getInstance()
                                    .collection("user_gallery")
                                    .document(uid)
                                    .collection("your_posts")
                                    .add(savedData)
                                    .addOnSuccessListener(docRef -> {
                                        Log.i("Firebase", "Post successfully added to user's posts!");
                                        Snackbar.make(findViewById(android.R.id.content).getRootView(),
                                                "Post created!", 3000).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase", "Error adding post to user's posts", e);
                                    });

                            //Store copy of post image
                            StorageReference postImageRef = FirebaseStorage.getInstance()
                                    .getReference()
                                    .child("post_images")
                                    .child(uid)
                                    .child(dRef.getId() + ".jpg");
                            Log.i("Firebase", "dref Id: " + dRef.getId());

                            // Download to temp file
                            try {
                                File localFile = File.createTempFile("tempImage", ".jpg");
                                new Thread(() -> {
                                    try (InputStream in = new URL(newPost.getImageUrl()).openStream();
                                         OutputStream out = new FileOutputStream(localFile)) {
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        while ((bytesRead = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, bytesRead);
                                        }

                                        Uri fileUri = Uri.fromFile(localFile);
                                        postImageRef.putFile(fileUri)
                                                .addOnSuccessListener(taskSnapshot -> postImageRef.getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {
                                                            dRef.update("imageUrl", String.valueOf(Uri.parse(uri.toString())))
                                                                    .addOnSuccessListener(v -> {
                                                                        Log.i("Firebase", "Changed post url field to copy");
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.e("Firebase", "Error changing post url field to copy", e);
                                                                    });
                                                            Log.i("Firebase", "Post image copy made");
                                                        }))
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firebase", "Error making post image copy", e);
                                                });
                                    } catch (IOException e) {
                                        Log.e("Firebase", "Error making post image copy, input stream", e);
                                    }
                                }).start();
                            } catch (IOException e) {
                                Log.e("Firebase", "Error making post image copy, temp file", e);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error adding post to feed", e);
                        });

            }
        }
    }

    private void setupKeyboardDismissOnTouch(View rootView) {
        if (!(rootView instanceof EditText)) {
            rootView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                    View focusedView = getCurrentFocus();
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}

/*
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagCasual"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Casual"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagFormal"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Formal"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagStreetwear"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Streetwear"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagSporty"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Sporty"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagVintage"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Vintage"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagChic"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Chic"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagBaggy"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Baggy"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagRetro"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Retro"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagOldMoney"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Old Money"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
                <com.google.android.material.chip.Chip
                    android:id="@+id/tagBusinessCasual"
                    style="@style/Widget.MaterialComponents.Chip.Filter"
                    android:text="Business Casual"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:focusable="true"
                    android:checkable="true"
                    android:clickable="true"
                    app:checkedIconVisible="true" />
 */