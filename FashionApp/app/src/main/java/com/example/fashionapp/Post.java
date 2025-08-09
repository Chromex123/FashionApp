package com.example.fashionapp;

import java.util.ArrayList;
import java.util.List;

public class Post {
    public String title;
    public String imageUrl;
    public String caption;
    public List<String> styles;
    public long timestamp;

    public Post() {} // Needed for Firebase

    public Post(String title, String imageUrl, ArrayList<String> styles, String caption, long timestamp) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.styles = styles;
        this.caption = caption;
        this.timestamp = timestamp;
    }
}
