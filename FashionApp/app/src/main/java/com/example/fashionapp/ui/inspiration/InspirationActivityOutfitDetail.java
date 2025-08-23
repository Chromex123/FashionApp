package com.example.fashionapp.ui.inspiration;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fashionapp.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Objects;

/**
 * InspirationActivityOutfitDetail displays a larger view of the user's selected post image when
 * clicked on.
 */
public class InspirationActivityOutfitDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_inspiration_outfit_detail);

        ImageView outfitDetail = (ImageView) findViewById(R.id.outfit_detail);
        Intent prevIntent = getIntent();
        String imageUrl = prevIntent.getStringExtra("imageUrl");

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.inspiration_default_image)
                .into(outfitDetail);
    }

}