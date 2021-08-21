package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class CheckIn extends Activity{

    private @DocumentId String id;
    private @ServerTimestamp Date checked;
    private DocumentReference toiletID;
    private String toiletLocation;
    private DocumentReference userID;
    private String userName;

    public CheckIn(){

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getChecked() {
        return checked;
    }
    public Date getDate() {
        return checked;
    }

    public String getCheckedString(){
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return df.format(checked);
    }

    public void setTime(Date checked) {
        this.checked = checked;
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

    public String getToiletLocation() {
        return toiletLocation;
    }

    public void setToiletLocation(String toiletLocation) {
        this.toiletLocation = toiletLocation;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "id='" + id + '\'' +
                ", checked=" + checked +
                ", toiletID=" + toiletID +
                ", toiletLocation='" + toiletLocation + '\'' +
                ", userID=" + userID +
                ", userName='" + userName + '\'' +
                '}';
    }
}
