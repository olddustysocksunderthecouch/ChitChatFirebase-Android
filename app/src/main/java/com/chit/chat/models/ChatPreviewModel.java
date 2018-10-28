package com.chit.chat.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by Adria on 2017/02/06.
 */

public class ChatPreviewModel {

    //String chatID;
    String last_message;
    String uid;
    String profilePic;
    String sender_name;
    String receiverrole;
    String status;
    int count;
    HashMap<String, Object>  timestampmessage;


    public ChatPreviewModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }



    public ChatPreviewModel(String last_message, String uid, String profilePic, String sender_name, String receiverrole, String status, int count
                          ) {

        this.last_message = last_message;
        this.uid = uid;
        this.profilePic = profilePic;
        this.sender_name = sender_name;
        this.receiverrole = receiverrole;
        this.status = status;
        this.count = count;

        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampmessage = timestampNow;

    }

    public String getSender_name() {
        return sender_name;
    }

    public int getCount() {
        return count;
    }

    public String getUid() {
        return uid;
    }

    public String getprofilePic() {return profilePic;}

    public String getLast_message() {
        return last_message;
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