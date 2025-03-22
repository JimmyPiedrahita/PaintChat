package com.jimmypiedrahita.chat;
public class Message {
    private String urlMessage;
    private String senderMessage;
    private String idChat;
    public Message() {}
    public Message(String urlMessage, String senderMessage, String idChat) {
        this.urlMessage = urlMessage;
        this.senderMessage = senderMessage;
        this.idChat = idChat;
    }
    public String getUrlMessage() {
        return urlMessage;
    }
    public void setUrlMessage(String urlMessage) {
        this.urlMessage = urlMessage;
    }
    public String getSenderMessage() {
        return senderMessage;
    }
    public void setSenderMessage(String senderMessage) {
        this.senderMessage = senderMessage;
    }
    public String getIdChat() {
        return idChat;
    }
    public void setIdChat(String receiverMessage) {
        this.idChat = receiverMessage;
    }
}
