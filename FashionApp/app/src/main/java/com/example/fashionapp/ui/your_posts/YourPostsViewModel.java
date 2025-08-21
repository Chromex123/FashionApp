package com.example.fashionapp.ui.your_posts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class YourPostsViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public YourPostsViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}