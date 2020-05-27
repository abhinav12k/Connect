package com.mass.connect;

/**
 * Created by abhinav on 23/5/20.
 */
public class Messages {

    private String message,type;
    private boolean seen;
    private long time;
    private String from;

    public Messages(String message, String type, boolean seen, long time){

        this.message = message;
        this.type = type;
        this.seen = seen;
        this.time = time;

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(){

    }

    public Messages(String from){

        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
