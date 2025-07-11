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

import com.example.fashionapp.R;

import java.util.Objects;

public class HomeActivityDetail extends AppCompatActivity {

    private ImageView recycleImage;
    Uri selectedRecycling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_home_detail);

        setUpViews();
        getRecycling();

    }

    private void setUpViews() {
        recycleImage = (ImageView) findViewById(R.id.photo_detail);

    }

    private void getRecycling() {
        Intent prevIntent = getIntent();
        int position = prevIntent.getIntExtra("position", 0);
        selectedRecycling = HomeFragment.recyclingArrayList.get(position);
        recycleImage.setImageURI(selectedRecycling);
    }
}