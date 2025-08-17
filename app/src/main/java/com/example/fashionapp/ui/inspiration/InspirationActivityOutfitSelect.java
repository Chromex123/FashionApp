package com.example.fashionapp.ui.inspiration;

import static java.sql.DriverManager.println;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionapp.OutfitAdapter;
import com.example.fashionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InspirationActivityOutfitSelect extends AppCompatActivity {

    FirebaseAuth mAuth;
    private RecyclerView outfitRecyclerView;
    private OutfitAdapter outfitAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private int selectedImageUrlPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspiration_outfit_select);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                if(selectedImageUrlPosition == RecyclerView.NO_POSITION) {
                    resultIntent.putExtra("selectedImageUrl", "");
                }else{
                    resultIntent.putExtra("selectedImageUrl", imageUrls.get(selectedImageUrlPosition));
                }
                Log.i("InspirationActivityOutfitSelect","Selected position: " + selectedImageUrlPosition);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        outfitRecyclerView = findViewById(R.id.outfitRecyclerView);
        outfitRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        outfitAdapter = new OutfitAdapter(imageUrls, pos -> selectedImageUrlPosition = pos, getApplicationContext());
        outfitRecyclerView.setAdapter(outfitAdapter);

        loadUserImages();
    }

    private void loadUserImages() {
        String uid = (Objects.requireNonNull(mAuth.getCurrentUser())).getUid();
        FirebaseFirestore.getInstance()
                .collection("user_gallery")
                .document(uid)
                .collection("images")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String url = doc.getString("imageUrl");
                        imageUrls.add(url);
                        Log.i("Firebase", "Image loaded");
                    }
                    outfitAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.i("Firebase", Objects.requireNonNull(e.getMessage()));
                });
    }
}