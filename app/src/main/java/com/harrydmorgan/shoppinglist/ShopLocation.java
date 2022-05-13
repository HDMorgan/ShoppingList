package com.harrydmorgan.shoppinglist;

import android.location.Location;

import java.util.Calendar;

public class ShopLocation {
    private final long id;
    private final String name;
    private final Calendar calendar;
    private double latitude;
    private double longitude;

    public ShopLocation(long id, String name, Calendar date, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.calendar = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {return name;}

    public long getId() {
        return id;
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public boolean isFarAway(Location location) {
        Location shopLocation = new Location(location);
        shopLocation.setLatitude(latitude);
        shopLocation.setLongitude(longitude);
        return shopLocation.distanceTo(location) > 150;
    }
}
