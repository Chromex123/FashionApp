package com.example.fashionapp.ui.inspiration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InspirationActivityPost extends AppCompatActivity {

    private boolean outfitSelected = false;
    private Post newPost;
    private final String[] styles = {"Casual", "Formal", "Streetwear", "Baggy", "Sporty", "Vintage",
                                     "Chic", "Retro", "Old Money", "Business Casual"};
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
                                    newPost.imageUrl = null;
                                }else{
                                    // Use Glide or whatever you want
                                    Glide.with(this).load(imageUrl)
                                            .centerCrop()
                                            .into((ImageView) findViewById(R.id.selectedImageView));
                                    outfitSelected = true;
                                    newPost.imageUrl = imageUrl;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspiration_post);
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }); */

        newPost = new Post();

        setupKeyboardDismissOnTouch(findViewById(android.R.id.content).getRootView());

        // Handling onClick Buttons

        Button selectOutfitButton = findViewById(R.id.selectOutfitButton);
        selectOutfitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectOutfitButtonClick(v);
            }
        });

        ChipGroup chipGroup = findViewById(R.id.tagChipGroup);
        for (String tag : styles) {
            Chip chip = new Chip(this);

            chip.setText(tag);
            chip.setCheckable(true);                // must be checkable for toggle
            chip.setClickable(true);                // clickable to receive taps
            chip.setCheckedIconVisible(true);      // show icon only when checked

            chipGroup.addView(chip);
        }

        // Optional: listen for selection changes
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedTags = new ArrayList<>();
            for (int id : checkedIds) {
                Chip selectedChip = group.findViewById(id);
                selectedTags.add(selectedChip.getText().toString());
            }
            Log.i("InspirationActivityPost", "Current styles: " + selectedTags);
        });

        Button submitPostButton = findViewById(R.id.submitPostButton);
        // Get all selected tags when needed (e.g., on button click)
        submitPostButton.setOnClickListener(v -> {
            List<Integer> selectedIds = chipGroup.getCheckedChipIds();
            List<String> selectedTags = new ArrayList<>();

            for (int id : selectedIds) {
                Chip chip = chipGroup.findViewById(id);
                selectedTags.add(chip.getText().toString());
            }

            Log.i("InspirationActivityPost", "Styles: " + selectedTags);
        });
    }

    private void onSelectOutfitButtonClick(View clicked) {
        Log.i("InspirationActivityPost","Button click");
        ((Button) clicked).setEnabled(false);
        Intent intent = new Intent(getApplicationContext(), InspirationActivityOutfitSelect.class);
        launcher.launch(intent);
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