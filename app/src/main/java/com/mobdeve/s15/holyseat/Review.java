package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

public class Review {

    private @DocumentId String id;
    private String details;
    private long numUpvotes;
    private @ServerTimestamp Timestamp posted;
    private long rating;
    private DocumentReference reviewerID;
    private DocumentReference toiletID;

    public Review(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getNumUpvotes() {
        return numUpvotes;
    }

    public void setNumUpvotes(long numUpvotes) {
        this.numUpvotes = numUpvotes;
    }

    public Timestamp getPosted() {
        return posted;
    }

    public void setPosted(Timestamp posted) {
        this.posted = posted;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public DocumentReference getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(DocumentReference reviewerID) {
        this.reviewerID = reviewerID;
    }

    public DocumentReference getToiletID() {
        return toiletID;
    }

    public void setToiletID(DocumentReference toiletID) {
        this.toiletID = toiletID;
    }

    @Override
    public String toString() {
        return "Review{" +
                "details='" + details + '\'' +
                ", numUpvotes=" + numUpvotes +
                ", posted=" + posted +
                ", rating=" + rating +
                ", reviewerID=" + reviewerID.getId() +
                ", toiletID=" + toiletID.getId() +
                '}';
    }
}
