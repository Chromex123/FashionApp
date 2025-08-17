package com.example.fashionapp.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionapp.Post;
import com.example.fashionapp.R;
import com.example.fashionapp.RecycleAdapter;
import com.example.fashionapp.Recycling;
import com.example.fashionapp.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment implements RecycleAdapter.OnItemListener, RecycleAdapter.OnItemLongClickListener{

    private FirebaseAuth mAuth;
    private FragmentHomeBinding binding;
    private ImageView selectedImageView;

    RecyclerView recyclerView;
    public static List<String> recyclingArrayList;
    RecycleAdapter recycleAdapter;

    private ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri imageUri = data.getData();
                                if(!recyclingArrayList.contains(imageUri)) {
                                    selectedImageView.setImageURI(imageUri);
                                    saveImageToFirebase(imageUri);
                                    //saveImageUrisToPreferences(recyclingArrayList);
                                    //Snackbar.make(binding.getRoot(), "Image uploaded", 500).show();
                                }else{
                                    Snackbar.make(binding.getRoot(), "Upload failed. Image already exists.", 1000).show();
                                }

                            }
                        }
                    }
            );

    boolean isInSelectionMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Handling onClick Buttons
        ImageButton addPhotoButton = root.findViewById(R.id.addPhotoButton);

        /// //////////////////////////////
        //clearPreferences(); // To reset all images stored in preferences
        /// /////////////////////////////

        //Set up grid view of images
        recyclingArrayList = new ArrayList<>();
        recycleAdapter = new RecycleAdapter(requireContext().getApplicationContext(), recyclingArrayList, this, this);
        setUpRecycler(root);
        Log.i("HomeFragment","Recycler Setup");

        View inflatedView = getLayoutInflater().inflate(R.layout.recycle_cell, null);
        selectedImageView = inflatedView.findViewById(R.id.photo_1);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPhotoButtonClick(v, root);
            }
        });

        // Load current images saved in Firestore
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadGalleryImages(); // safe to call
        } else {
            mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    loadGalleryImages();
                })
                .addOnFailureListener(e -> {
                    Log.i("Firebase", "Anonymous sign-in failed", e);
                });
        }
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recycleAdapter.clear();
        binding = null;
    }

    private void setUpRecycler(View root) {
        recyclerView = (RecyclerView) root.findViewById(R.id.recycle_view);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext().getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recycleAdapter);
    }

    private void saveImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("user_gallery")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(UUID.randomUUID().toString() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            saveImageUrlToFirestore(uri);
                            recyclingArrayList.add(String.valueOf(Uri.parse(uri.toString())));
                            recycleAdapter.notifyDataSetChanged();
                            Log.i("HomeFragment","Image added");
                        }))
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.getRoot(), "Upload failed", 500).show();
                });
    }

    private void saveImageUrlToFirestore(Uri imageUri) {
        String imageUrl = String.valueOf(Uri.parse(imageUri.toString()));
        String uid = (Objects.requireNonNull(mAuth.getCurrentUser())).getUid();
        Log.i("Firebase", "Saved to firebase");

        Map<String, Object> newImage = new HashMap<>();
        newImage.put("imageUrl", imageUrl);
        newImage.put("uid", uid);
        newImage.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("user_gallery")
                .document(uid)
                .collection("images")
                .add(newImage)
                .addOnSuccessListener(documentReference -> {
                    Log.i("Firebase", "Url saved to firestore");
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Firebase", Objects.requireNonNull(e.getMessage()));
                    }
                });
    }

    private void loadGalleryImages() {
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();

        FirebaseFirestore.getInstance()
                .collection("user_gallery")
                .document(uid)
                .collection("images")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 1;
                    for (DocumentSnapshot doc : querySnapshot) {
                        String imageUrl = doc.getString("imageUrl");
                        recyclingArrayList.add(imageUrl);
                        Log.i("Firebase", "Image loaded");
                    }
                    recycleAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.i("Firebase", Objects.requireNonNull(e.getMessage()));
                });
    }

    private void deleteImagesFromFirebase(List<String> toDelete) {
        String uid = (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();

        //Delete from firestore
        for (String imageUrl : toDelete) {
            FirebaseFirestore.getInstance()
                .collection("user_gallery")
                .document(uid)
                .collection("images") // skip if your structure is flat
                .whereEqualTo("imageUrl", imageUrl)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Delete the document
                        doc.getReference().delete()
                            .addOnSuccessListener(aVoid -> Log.i("Firebase", "Deleted from Firestore"))
                            .addOnFailureListener(e -> Log.e("Firebase", "Delete failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Query failed: " + e.getMessage()));

            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                .delete()
                .addOnSuccessListener(aVoid -> Log.i("Storage", "Deleted from Firebase"))
                .addOnFailureListener(e -> Log.e("Storage", "Storage delete failed: " + e.getMessage()));
        }
    }

    private void saveImageUrisToPreferences(List<Uri> imageUris) {
        SharedPreferences prefs = requireContext().getApplicationContext().getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert each Uri to string
        Set<String> uriStrings = new HashSet<>();
        for (Uri uri : imageUris) {
            uriStrings.add(uri.toString());
        }

        editor.putStringSet("saved_uris", uriStrings);
        editor.commit();
    }

    private void onAddPhotoButtonClick(View clicked, View view) {
        Log.i("HomeFragment","Button click");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        launcher.launch(intent);
        //saveImageUrisToPreferencesTest();
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(requireContext().getApplicationContext(), HomeActivityDetail.class);
        detailIntent.putExtra("position", position);
        startActivity(detailIntent);
        Log.i("HomeFragment", "Item Click Activity Started, Clicked image at " + position);
    }

    @Override
    public void onItemLongClick() {
        Log.i("HomeFragment", "Multi-Select Mode");
        this.isInSelectionMode = true;
        showBottomActionBar();
        toggleAddPhotoButton(false);
        TextView badge = binding.getRoot().findViewById(R.id.selection_count_badge);
        badge.setVisibility(View.VISIBLE);

        LinearLayout cancelButton = binding.getRoot().findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            recycleAdapter.clearSelection();
            this.isInSelectionMode = false;
            hideBottomActionBar();
            toggleAddPhotoButton(true);
            badge.setVisibility(View.GONE);
            badge.setText(1 + " selected");
        });

        LinearLayout deleteButton = binding.getRoot().findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            List<String> selectedUris = recycleAdapter.getSelectedUris();

            if (selectedUris.isEmpty()) {
                return;
            }

            new AlertDialog.Builder(requireContext())
                .setTitle("Delete selected images?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {

                    // 1. Remove selected images from imageUris
                    recyclingArrayList.removeAll(selectedUris);
                    Log.i("HomeFragment", selectedUris.size() + " images deleted");

                    // 2. Delete selected images from firestore
                    try {
                        deleteImagesFromFirebase(selectedUris);
                    } catch (Exception e) {
                        Log.e("HomeFragment", "Error deleting images");
                        Snackbar.make(binding.getRoot(), "Could not delete image(s). Try again.", 1000).show();
                    }

                    // 3. Update adapter
                    recycleAdapter.clearSelection();
                    recycleAdapter.notifyDataSetChanged();

                    // 4. Hide selection UI and badge
                    hideBottomActionBar();
                    isInSelectionMode = false;
                    if (badge != null) {
                        badge.setVisibility(View.GONE);
                        badge.setText(1 + " selected");
                    }

                    // Make add button visible again
                    toggleAddPhotoButton(true);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Just close the dialog
                })
                .show();

        });

        recycleAdapter.setOnSelectionChangedListener((isStillInSelectionMode, count) -> {
            if (badge == null) return;
            if (!isStillInSelectionMode) {
                isInSelectionMode = false;
                hideBottomActionBar();
                toggleAddPhotoButton(true);
                badge.setVisibility(View.GONE);
            }else{
                badge.setVisibility(View.VISIBLE);
                badge.setText(count + " selected");
            }
        });
    }

    private void showBottomActionBar() {
        View bar = binding.getRoot().findViewById(R.id.bottom_action_bar);
        if (bar.getVisibility() != View.VISIBLE) {
            bar.setAlpha(0f);
            bar.setTranslationY(bar.getHeight()); // start off screen
            bar.setVisibility(View.VISIBLE);

            bar.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(350)
                    .start();
        }
    }
    private void hideBottomActionBar() {
        View bar = binding.getRoot().findViewById(R.id.bottom_action_bar);
        if (bar.getVisibility() == View.VISIBLE) {
            bar.animate()
                    .alpha(0f)
                    .translationY(bar.getHeight())
                    .setDuration(350)
                    .withEndAction(() -> bar.setVisibility(View.GONE))
                    .start();
        }
    }

    private void toggleAddPhotoButton(boolean show) {
        View actionBar = binding.getRoot().findViewById(R.id.addPhotoButton);
        if (actionBar != null) {
            actionBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /*
     ********************
     ********************
     * Currently unused *

    private List<Uri> loadPostsFromFirestore() {
        List<Uri> images = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("user_gallery")
                .whereEqualTo("uid", (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        images.add(doc.toObject(Uri.class));
                    }
                });
        Log.i("HomeFragment","Current uris: " + images);
        return images;
    }

    private List<Uri> loadImageUrisFromPreferences() {
        SharedPreferences prefs = requireContext().getApplicationContext().getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE);
        Set<String> uriStrings = prefs.getStringSet("saved_uris", new HashSet<>());
        List<Uri> uris = new ArrayList<>();

        for (String uriStr : uriStrings) {
            uris.add(Uri.parse(uriStr));
        }
        Log.i("HomeFragment","Current uris: " + uris);

        return uris;
    }

    // To test saving/loading from images in drawable folder
    private void saveImageUrisToPreferencesTest() {
        SharedPreferences prefs = requireContext().getApplicationContext().getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<Uri> imageUris = new ArrayList<>();

        imageUris.add(Uri.parse("android.resource://"+requireContext().getApplicationContext().getPackageName() + "/" + R.drawable.ic_home_add));
        imageUris.add(Uri.parse("android.resource://"+requireContext().getApplicationContext().getPackageName() + "/" + R.drawable.ic_menu_about));
        imageUris.add(Uri.parse("android.resource://"+requireContext().getApplicationContext().getPackageName() + "/" + R.drawable.ic_menu_home));

        // Convert each Uri to string
        Set<String> uriStrings = new HashSet<>();
        for (Uri uri : imageUris) {
            uriStrings.add(uri.toString());
            selectedImageView.setImageURI(uri);
            recyclingArrayList.add(uri);
            recycleAdapter.notifyDataSetChanged();
        }

        editor.putStringSet("saved_uris", uriStrings);
        editor.commit();
        Log.i("HomeFragment","Saved Test");
    }

    private void clearPreferences() {
        SharedPreferences prefs = requireContext().getApplicationContext().getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("saved_uris");
        editor.commit();
    }
    */
}