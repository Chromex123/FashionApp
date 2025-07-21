package com.example.fashionapp;

public class Post {
    public String imageUrl;
    public String caption;
    public long timestamp;

    public Post() {} // Needed for Firebase

    public Post(String imageUrl, String caption, long timestamp) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
    }
}
