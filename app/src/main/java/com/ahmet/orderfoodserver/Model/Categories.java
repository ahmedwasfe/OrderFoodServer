package com.ahmet.orderfoodserver.Model;

import java.security.Timestamp;

public class Categories {

    private String name, image;
    private long timestamp;


    public Categories() {
    }

    public Categories(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public Categories(String name, String image, long timestamp) {
        this.name = name;
        this.image = image;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
