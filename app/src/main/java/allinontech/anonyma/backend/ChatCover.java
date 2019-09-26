package allinontech.anonyma.backend;

public class ChatCover {

    String senderName;
    String lastMessage;
    String chatId;
    String timeOfChat;

    public ChatCover(String senderName, String lastMessage, String timeOfChat, String chatId){
        this.senderName = senderName;
        this.lastMessage = lastMessage;
        this.timeOfChat = timeOfChat;
        this.chatId = chatId;
    }
    public ChatCover(){
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {

        return chatId;
    }

    public void setSenderName(String senderUid) {
        this.senderName = senderUid;
    }


    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTimeOfChat(String timeOfChat) {
        this.timeOfChat = timeOfChat;
    }

    public String getSenderName() {

        return senderName;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimeOfChat() {
        return timeOfChat;
    }
}
