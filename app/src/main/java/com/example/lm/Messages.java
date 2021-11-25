package com.example.lm;

public class Messages {

    String message, senderId, currenttime;
    boolean seen;
    long timestamp;


    public Messages() {
    }


    public Messages(String message, String senderId, long timestamp, String currenttime, boolean seen) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.currenttime = currenttime;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrenttime() {
        return currenttime;
    }
    public void setCurrenttime(String currenttime) {
        this.currenttime = currenttime;
    }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}
