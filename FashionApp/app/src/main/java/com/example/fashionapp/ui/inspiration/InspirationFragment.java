package com.example.fashionapp.ui.inspiration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fashionapp.R;
import com.example.fashionapp.databinding.FragmentInspirationBinding;
import com.example.fashionapp.ui.home.HomeActivityDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InspirationFragment extends Fragment {

    private FragmentInspirationBinding binding;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        InspirationViewModel inspirationViewModel =
                new ViewModelProvider(this).get(InspirationViewModel.class);

        binding = FragmentInspirationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        super.onViewCreated(root, savedInstanceState);

        setupKeyboardDismissOnTouch(root);

        ImageButton newPostButton = root.findViewById(R.id.newPostButton);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewPostButtonClick(v, root);
            }
        });

        // Load current images saved in Firestore
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // call firebase function
        } else {
            mAuth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // call firebase function
                    })
                    .addOnFailureListener(e -> {
                        Log.i("Firebase", "Anonymous sign-in failed", e);
                    });
        }

        return root;
    }

    private void onNewPostButtonClick(View clicked, View view) {
        Log.i("InspirationFragment","Button click");
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
        binding = null;
    }
}