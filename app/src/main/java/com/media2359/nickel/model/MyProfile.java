package com.media2359.nickel.model;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.media2359.nickel.utils.PreferencesUtils;

/**
 * Created by Xijun on 31/3/16.
 */
public class MyProfile {

    public static final int STATUS_EMPTY = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_PENDING = 2;

    private static MyProfile myProfile;

    int id;
    @SerializedName("name")
    String fullName;

    String dateOfBirth;
    @SerializedName("address")
    String streetAddress;
    @SerializedName("city")
    String city;
    @SerializedName("postalCode")
    String postalCode;
    @SerializedName("documentType")
    String documentType;

    String documentID;

    String frontPhotoUri;
    String backPhotoUri;

    private MyProfile(Builder builder, Context context) {
        this.fullName = builder.fullName;
        this.dateOfBirth = builder.dateOfBirth;
        this.streetAddress = builder.streetAddress;
        this.city = builder.city;
        this.postalCode = builder.postalCode;
        this.documentType = builder.documentType;
        this.documentID = builder.documentID;
        this.frontPhotoUri = builder.frontPhotoUrl;
        this.backPhotoUri = builder.backPhotoUrl;

        myProfile = this;
        PreferencesUtils.saveProfile(context, myProfile);
    }

    /**
     * Gets the profile instance. Can be null
     *
     * @param context
     * @return The saved profile of the current user. Can be null
     */
    public static MyProfile getCurrentProfile(Context context) {
        if (myProfile == null) {
            myProfile = PreferencesUtils.getProfile(context);
        }
        return myProfile;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentID() {
        return documentID;
    }

    public String getBackPhotoUri() {
        return backPhotoUri;
    }

    public String getFrontPhotoUri() {
        return frontPhotoUri;
    }

    /**
     * A builder for the Profile class. Follows a fluent interface.
     */
    public static final class Builder {

        private String fullName;
        private String dateOfBirth;
        private String streetAddress;
        private String city;
        private String postalCode;
        private String documentType;
        private String documentID;
        private String frontPhotoUrl;
        private String backPhotoUrl;

        public Builder() {
        }

        public Builder withFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder withDOB(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder withDocumentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder withDocumentID(String documentID) {
            this.documentID = documentID;
            return this;
        }

        public Builder withFrontPhotoUrl(String frontPhotoUrl) {
            this.frontPhotoUrl = frontPhotoUrl;
            return this;
        }

        public Builder withBackPhotoUrl(String backPhotoUrl) {
            this.backPhotoUrl = backPhotoUrl;
            return this;
        }

        public MyProfile buildAndSave(Context context) {
            return new MyProfile(this, context);
        }

    }
}
