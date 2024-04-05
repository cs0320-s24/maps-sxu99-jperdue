package edu.brown.cs.student.main.server.handlers;

/**
 * Class for a boundary box
 */
public class CoordsBox {

    public double minLat;
    public double maxLat;
    public double minLng;
    public double maxLng;

    public CoordsBox(double minLat, double maxLat, double minLng, double maxLng){
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLng = minLng;
        this.maxLng = maxLng;
    }

}
