package com.example.lm;

public class userprofile {

    public String username,userUID, userStatus, typingTo;

    public userprofile() {
    }

    public userprofile(String username, String userUID, String userStatus, String typingTo) {
        this.username = username;
        this.userUID = userUID;
        this.userStatus = userStatus;
        this.typingTo = typingTo;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserUID() {
        return userUID;
    }
    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUserStatus() {
        return userStatus;
    }
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }
    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
