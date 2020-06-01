package com.mass.connect;

/**
 * Created by abhinav on 28/5/20.
 */
public class Conv {

    private static boolean seen;
    private long timestamp;

    public Conv(){

    }

    public Conv(boolean seen,long timestamp){
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public static boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
