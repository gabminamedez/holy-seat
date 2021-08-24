package com.mobdeve.s15.holyseat;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Toilet {

    private @DocumentId String id;
    private double avgRating;
    private double longitude;
    private double latitude;
    private String image;
    private @ServerTimestamp Date created;
    private String location;
    private long numCheckins;
    private long numReviews;
    private String roomType;
    private String toiletType;

    public Toilet(){

    }

    @Override
    public String toString() {
        return "Toilet{" +
                "id='" + id + '\'' +
                ", avgRating=" + avgRating +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", image='" + image + '\'' +
                ", created=" + created +
                ", location='" + location + '\'' +
                ", numCheckins=" + numCheckins +
                ", numReviews=" + numReviews +
                ", roomType='" + roomType + '\'' +
                ", toiletType='" + toiletType + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getNumCheckins() {
        return numCheckins;
    }

    public void setNumCheckins(long numCheckins) {
        this.numCheckins = numCheckins;
    }

    public long getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(long numReviews) {
        this.numReviews = numReviews;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getToiletType() {
        return toiletType;
    }

    public void setToiletType(String toiletType) {
        this.toiletType = toiletType;
    }

}
