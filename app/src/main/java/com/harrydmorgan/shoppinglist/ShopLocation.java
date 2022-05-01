package com.harrydmorgan.shoppinglist;

import java.util.Date;

public class ShopLocation {
    private long id;
    private String name;
    private Date date;
    private double latitude;
    private double longitude;

    public ShopLocation(long id, String name, Date date, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {return name;}

    public long getId() {
        return id;
    }
}
