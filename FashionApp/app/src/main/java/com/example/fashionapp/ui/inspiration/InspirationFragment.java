package com.example.fashionapp.ui.inspiration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fashionapp.databinding.FragmentInspirationBinding;

public class InspirationFragment extends Fragment {

    private FragmentInspirationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InspirationViewModel inspirationViewModel =
                new ViewModelProvider(this).get(InspirationViewModel.class);

        binding = FragmentInspirationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInspiration;
        inspirationViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}