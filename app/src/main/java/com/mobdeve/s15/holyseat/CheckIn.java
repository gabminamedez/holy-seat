package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;


public class CheckIn {

    private @DocumentId String id;
    private @ServerTimestamp Timestamp time;
    private DocumentReference toiletID;
    private DocumentReference userID;
    public CheckIn(){

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public DocumentReference getToiletID() {
        return toiletID;
    }

    public void setToiletID(DocumentReference toiletID) {
        this.toiletID = toiletID;
    }

    public DocumentReference getUserID() {
        return userID;
    }

    public void setUserID(DocumentReference userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "time=" + time +
                ", toiletID=" + toiletID.getId() +
                ", userID=" + userID.getId() +
                '}';
    }
}
