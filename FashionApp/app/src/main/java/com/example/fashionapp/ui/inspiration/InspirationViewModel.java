package com.example.fashionapp.ui.inspiration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InspirationViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public InspirationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Get fit inspiration from others!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}