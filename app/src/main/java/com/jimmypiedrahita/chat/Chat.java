package com.jimmypiedrahita.chat;

public class Chat {
    private String user1;
    private String user2;
    private String id;

    public Chat() {}

    public Chat(String user1, String user2, String id) {
        this.user1 = user1;
        this.user2 = user2;
        this.id = id;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
