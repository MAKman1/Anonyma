package allinontech.anonyma.backend;

public class singleComment {
    String text;
    String dateTime;
    String sentById;
    String sentByUser;

    public singleComment( String text, String dateTime, String sentById, String sentByUser){
        this.text = text;
        this.dateTime = dateTime;
        this.sentById = sentById;
        this.sentByUser = sentByUser;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setSentById(String sentById) {
        this.sentById = sentById;
    }

    public void setSentByUser(String sentByUser) {
        this.sentByUser = sentByUser;
    }

    public String getText() {

        return text;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getSentById() {
        return sentById;
    }

    public String getSentByUser() {
        return sentByUser;
    }
}
