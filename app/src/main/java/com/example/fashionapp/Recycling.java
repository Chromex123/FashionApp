package com.example.fashionapp;

public class Recycling {
    private String id;
    private String name;
    private String type;
    private int image;

    public Recycling(String id, String name, String type, int image) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
