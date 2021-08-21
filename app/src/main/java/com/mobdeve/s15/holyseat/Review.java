package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Review extends Activity{

    private @DocumentId String id;
    private String details;
    private long numUpvotes;
    private @ServerTimestamp Date posted;
    private long rating;
    private DocumentReference reviewerID;
    private String reviewerName;
    private DocumentReference toiletID;
    private String toiletLocation;

    public Review(){

    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getToiletLocation() {
        return toiletLocation;
    }

    public void setToiletLocation(String toiletLocation) {
        this.toiletLocation = toiletLocation;
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

    public Date getPosted() {
        return posted;
    }
    public String getPostedString(){
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return df.format(posted);
    }
    public Date getDate() {
        return posted;
    }

    public void setPosted(Date posted) {
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
