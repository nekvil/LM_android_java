package com.example.lm;

public class userRDModel {

    public String userName, userId, userStatus, typingTo;
    long lastOnline;

    public userRDModel() {
    }

    public userRDModel(String userName, String userId, String userStatus, String typingTo, long lastOnline) {
        this.userName = userName;
        this.userId = userId;
        this.userStatus = userStatus;
        this.typingTo = typingTo;
        this.lastOnline = lastOnline;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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

    public long getLastOnline() {
        return lastOnline;
    }
    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }
}
