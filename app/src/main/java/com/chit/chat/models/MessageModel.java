package com.chit.chat.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by Adria on 2017/02/10.
 */

public class MessageModel {

    String name;
    String text;
    String uid;
    String type;
    String url;
    String status;
    HashMap<String, Object>  timestamp;


    public MessageModel() {
    }

    public MessageModel(String name, String uid, String message, String type, String url, String status) {
        this.name = name;
        this.text = message;
        this.uid = uid;
        this.type = type;
        this.url = url;
        this.status = status;
//        HashMap<String, Object> timestampNow = new HashMap<>();
//        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
//        this.timestamp = timestampNow;
    }

    public String getName() {
        return name;
    }
    public String getUid() {
        return uid;
    }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getText() { return text;}

    public String getStatus() {
        return status;
    }

//    public HashMap<String, Object> getTimestamp() {
//        return timestamp;
//    }

    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestamp.get("timestamp");
    }


}