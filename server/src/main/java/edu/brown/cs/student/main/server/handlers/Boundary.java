package edu.brown.cs.student.main.server.handlers;

public class Boundary {
  private double minLat;
  private double maxLat;
  private double minLong;
  private double maxLong;

  public Boundary(double minLat, double maxLat, double minLong, double maxLong) {
    this.minLat = minLat;
    this.maxLat = maxLat;
    this.minLong = minLong;
    this.maxLong = maxLong;
  }

  public double getMinLat() {
    return minLat;
  }

  public double getMaxLat() {
    return maxLat;
  }

  public double getMinLong() {
    return minLong;
  }

  public double getMaxLong() {
    return maxLong;
  }
}
