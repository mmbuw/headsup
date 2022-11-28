package com.example.neginsharif.mdemo.notification;

public class Message {
    private boolean response;
    private long timestamp;

    public Message(boolean response) {
        this.response = response;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean getResponse() {
        return response;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
