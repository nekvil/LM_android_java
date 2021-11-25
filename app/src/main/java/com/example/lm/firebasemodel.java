package com.example.lm;

public class firebasemodel {

    String name;
    String image;
    String uid;

    public firebasemodel(String name, String image, String uid) {
        this.name = name;
        this.image = image;
        this.uid = uid;
    }

    public firebasemodel() {
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
