package com.edna.fooddonation.model;

public class Donation {
    private String userId;
    private String fullName;
    private String foodItem;
    private String phone;
    private String description;
    private Double longitude;
    private Double latitude;
    private boolean status;

    public Donation(String userId, String fullName, String foodItem, String phone, String description, Double longitude, Double latitude, boolean status) {
        this.userId = userId;
        this.fullName = fullName;
        this.foodItem = foodItem;
        this.phone = phone;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
    }

    public Donation() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
