package com.example.fashionapp.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.bumptech.glide.Glide;
import com.example.fashionapp.Post;
import com.example.fashionapp.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Objects;

/**
 * FavoritesActivityPostDetail displays a more detailed view of the post the user clicked on,
 * displaying a larger image and the post's description.
 */
public class FavoritesActivityPostDetail extends AppCompatActivity {
    private ImageView postImage;
    private TextView postCaption;
    private FlexboxLayout postStyles;
    Post selectedPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_favorites_post_detail);

        postImage = (ImageView) findViewById(R.id.selectedPostImage);
        postCaption = (TextView) findViewById(R.id.selectedPostCaption);
        postStyles = (FlexboxLayout) findViewById(R.id.selectedPostStyles);
        getPostInfo();
    }

    /**
     * Gets the image, styles, and caption of the selected post from the user's saved posts screen
     * to display.
     */
    private void getPostInfo() {
        Intent prevIntent = getIntent();
        int position = prevIntent.getIntExtra("position", 0);
        selectedPost = FavoritesFragment.savedPostsList.get(position);

        Glide.with(this)
                .load(selectedPost.getImageUrl())
                .placeholder(R.drawable.inspiration_default_image)
                .into(postImage);

        if (selectedPost.getStyles() != null) {
            for (String tag : selectedPost.getStyles()) {
                TextView tagView = new TextView(this);
                tagView.setText(tag);
                tagView.setTextSize(10);
                tagView.setPadding(24, 4, 24, 4);
                tagView.setBackgroundResource(R.drawable.bg_inspiration_styles);
                tagView.setTextColor(ContextCompat.getColorStateList(this, R.color.white));

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 8, 8, 8);
                tagView.setLayoutParams(params);

                tagView.setClickable(false);

                postStyles.addView(tagView);
            }
        }

        postCaption.setText(selectedPost.getCaption());
    }
}