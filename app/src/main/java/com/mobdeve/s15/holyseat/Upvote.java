package com.mobdeve.s15.holyseat;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

public class Upvote {

    private @DocumentId String id;
    private DocumentReference reviewID;
    private DocumentReference userID;

    public Upvote(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getReviewID() {
        return reviewID;
    }

    public void setReviewID(DocumentReference reviewID) {
        this.reviewID = reviewID;
    }

    public DocumentReference getUserID() {
        return userID;
    }

    public void setUserID(DocumentReference userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "Upvote{" +
                "id='" + id + '\'' +
                ", reviewID=" + reviewID.getId() +
                ", userID=" + userID.getId() +
                '}';
    }
}
