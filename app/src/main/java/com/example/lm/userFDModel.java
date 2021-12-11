package com.example.lm;

public class userFDModel {

    String name;
    String image;
    String uid;

    public userFDModel(String name, String image, String uid) {
        this.name = name;
        this.image = image;
        this.uid = uid;
    }

    public userFDModel() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

}
