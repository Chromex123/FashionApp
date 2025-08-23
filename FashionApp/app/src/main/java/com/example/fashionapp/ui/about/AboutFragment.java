package com.example.fashionapp.ui.about;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fashionapp.R;
import com.example.fashionapp.databinding.FragmentAboutBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * AboutFragment displays information about the app itself, including its purpose,
 * copyright details, terms of service, and privacy policy. It provides
 * buttons for users to view these details in dialog popups.
 */
public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    private FirebaseAuth mAuth;
    private final String copyrightStr = "© 2025 Fashionly. All rights reserved.";
    private final String tosStr = "By using this app, you agree to abide by our terms. " +
            "You may not misuse our services or violate any applicable laws.";
    private final String privacyStr = "We respect your privacy. Our app is fully anonymous — we do not collect names, emails, or personal details.\n" +
            "The only information we collect is the images you choose to upload.\n" +
            "We use these images only to display in your public posts in the app.\n" +
            "You can delete your posts at any time, and the associated image will be removed from public view.\n" +
            "We do not sell or share your content with third parties, except if required by law.";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button copyrightButton = root.findViewById(R.id.copyrightButton);
        Button tosButton = root.findViewById(R.id.termsButton);
        Button privacyButton= root.findViewById(R.id.privacyButton);

        copyrightButton.setOnClickListener(v -> showDialog("Copyright", copyrightStr));

        tosButton.setOnClickListener(v -> showDialog("Terms of Service", tosStr));

        privacyButton.setOnClickListener(v -> showDialog("Privacy Policy", privacyStr));

        mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    //Log.i("Firebase", "Anonymous sign-in success in About");
                })
                .addOnFailureListener(e -> {
                    //Log.i("Firebase", "Anonymous sign-in failed", e);
                });

        return root;
    }

    /**
     * Shows a dialog pop-up displaying detailed information about the button clicked on
     */
    private void showDialog(String title, String message) {
        if (getActivity() == null) return;

        new AlertDialog.Builder(getActivity(), R.style.DefaultAlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}