package com.example.fashionapp;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post {
    private String docId;
    private String title;
    private String imageUrl;
    private String caption;
    private List<String> styles;
    private long timestamp;
    private String uid;
    private long likes;
    private long dislikes;
    private long voteCount; // likes - dislikes
    Map<String, Long> votesMap;

    public Post() {} // Needed for Firebase

    public Post(String uid, String title, String caption, String imageUrl, List<String> styles, long timestamp) {
        this.docId = "";
        this.uid = uid;
        this.title = title;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.styles = styles;
        this.timestamp = timestamp;
        this.likes = 0;
        this.dislikes = 0;
        this.voteCount = 0;
        this.votesMap = new HashMap<String, Long>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long votes) {
        this.voteCount = votes;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Map<String, Long> getVotesMap() {
        return votesMap;
    }

    public void setVotesMap(Map<String, Long> votesMap) {
        this.votesMap = votesMap;
    }

    @NonNull
    @Override
    public String toString() {
        return "\nTitle: " + this.title + "\nUrl: " + this.imageUrl
                + "\nCaption: " + this.caption +
                "\nStyles: " + this.styles + "\nTimestamp: " + this.timestamp;
    }
}
