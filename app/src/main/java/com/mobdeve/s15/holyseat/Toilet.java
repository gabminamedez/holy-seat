package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class Toilet {

    private @DocumentId String id;
    private long avgRating;
    private GeoPoint coordinates;
    private String image;
    private @ServerTimestamp Timestamp created;
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
                ", coordinates=" + coordinates +
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

    public long getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(long avgRating) {
        this.avgRating = avgRating;
    }

    public GeoPoint getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeoPoint coordinates) {
        this.coordinates = coordinates;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
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

    public Toilet(String id, long avgRating, GeoPoint coordinates, String image, Timestamp created, String location, long numCheckins, long numReviews, String roomType, String toiletType) {
        this.id = id;
        this.avgRating = avgRating;
        this.coordinates = coordinates;
        this.image = image;
        this.created = created;
        this.location = location;
        this.numCheckins = numCheckins;
        this.numReviews = numReviews;
        this.roomType = roomType;
        this.toiletType = toiletType;
    }
}
