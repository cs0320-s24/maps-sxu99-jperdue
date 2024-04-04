package edu.brown.cs.student.main.server.handlers;

public class Region {
  private Boundary boundary;

  public Region(Boundary boundary) {
    this.boundary = boundary;
  }

  public Boundary getBoundary() {
    return this.boundary;
  }
}
