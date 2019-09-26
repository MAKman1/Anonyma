package allinontech.anonyma.backend;

public class Chat {
    boolean me;
    String chat;
    String time;

    public Chat(boolean me, String chat, String time){
        this.me = me;
        this.chat = chat;
        this.time = time;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMe() {

        return me;
    }

    public String getChat() {
        return chat;
    }

    public String getTime() {
        return time;
    }
}
