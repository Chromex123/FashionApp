package com.example.fashionapp.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fashionapp.R;

import java.util.Objects;

/**
 * HomeActivityDetail displays a larger view of the image the user clicked on.
 */
public class HomeActivityDetail extends AppCompatActivity {

    private ImageView recycleImage;
    String selectedRecycling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_home_detail);

        recycleImage = (ImageView) findViewById(R.id.photo_detail);
        getRecycling();

    }

    /**
     * Display a larger view of the image the user clicked on in their uploaded images screen.
     */
    private void getRecycling() {
        Intent prevIntent = getIntent();
        int position = prevIntent.getIntExtra("position", 0);
        selectedRecycling = HomeFragment.recyclingArrayList.get(position);
        Glide.with(this)
                .load(selectedRecycling)
                .into(recycleImage);
    }
}