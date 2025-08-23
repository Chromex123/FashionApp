package com.example.fashionapp.ui.your_posts;

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
import com.example.fashionapp.Post;
import com.example.fashionapp.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Objects;

/**
 * YourPostsActivityPostDetail displays a more detailed view of the post the user clicked on,
 * displaying a larger image and the post's description.
 */
public class YourPostsActivityPostDetail extends AppCompatActivity {
    private ImageView postImage;
    private TextView postCaption;
    private FlexboxLayout postStyles;
    Post selectedPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_posts_post_detail);
        Objects.requireNonNull(getSupportActionBar()).hide();

        postImage = (ImageView) findViewById(R.id.selectedPostImage);
        postCaption = (TextView) findViewById(R.id.selectedPostCaption);
        postStyles = (FlexboxLayout) findViewById(R.id.selectedPostStyles);
        getPostInfo();
    }

    private void getPostInfo() {
        Intent prevIntent = getIntent();
        int position = prevIntent.getIntExtra("position", 0);
        selectedPost = YourPostsFragment.yourPostsList.get(position);

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

                // Optional: margin
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 8, 8, 8);
                tagView.setLayoutParams(params);

                // Make selectable
                tagView.setClickable(false);

                postStyles.addView(tagView);
            }
        }

        postCaption.setText(selectedPost.getCaption());
    }
}