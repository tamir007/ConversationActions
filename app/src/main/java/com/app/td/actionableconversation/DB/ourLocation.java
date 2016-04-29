package com.app.td.actionableconversation.DB;

import java.io.Serializable;

/**
 * Created by user on 14/02/2016.
 */
public class ourLocation implements Serializable{

    public double getLat() {
        return lat;
    }

    public double getLonge() {
        return longe;
    }

    private double lat;
    private double longe;

    public ourLocation(double lat, double longe) {
        this.lat = lat;
        this.longe = longe;
    }
}
