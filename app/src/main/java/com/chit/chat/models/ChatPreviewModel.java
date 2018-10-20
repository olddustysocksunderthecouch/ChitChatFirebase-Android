package com.voting.group.dev.googel.chitchat.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by Adria on 2017/02/06.
 */

public class ChatPreviewModel {

    //String chatID;
    String message;
    String uid;
    String profilePic;
    String name;
    String receiverrole;
    String status;
    int count;
    HashMap<String, Object>  timestampmessage;


    public ChatPreviewModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }



    public ChatPreviewModel(String message, String uid, String profilePic, String name, String receiverrole, String status, int count
                          ) {

        this.message = message;
        this.uid = uid;
        this.profilePic = profilePic;
        this.name = name;
        this.receiverrole = receiverrole;
        this.status = status;
        this.count = count;

        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampmessage = timestampNow;

    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public String getUid() {
        return uid;
    }

    public String getprofilePic() {return profilePic;}

    public String getMessage() {
        return message;
    }

    public String getReceiverrole() { return receiverrole; }

    public String getStatus() {
        return status;
    }

    public HashMap<String, Object> getTimestampmessage() {
        return timestampmessage;
    }

    @Exclude
    public long getTimestampMessage(){
        return (long)timestampmessage.get("timestamp");
    }


}