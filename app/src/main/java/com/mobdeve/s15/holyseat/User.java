package com.mobdeve.s15.holyseat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class User {

    private @DocumentId String id;
    private @ServerTimestamp Timestamp created;
    private String displayName;
    private String email;
    private long numReviews;
    private String username;
    private String password;
    public User(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(long numReviews) {
        this.numReviews = numReviews;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", numReviews=" + numReviews +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
