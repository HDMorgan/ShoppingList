package com.harrydmorgan.shoppinglist;

public class ShopLocation {
    private int id;
    private String name;
    private float latitude;
    private float longitude;

    public ShopLocation(int id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
