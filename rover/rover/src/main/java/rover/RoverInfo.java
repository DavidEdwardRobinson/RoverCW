package rover;

import java.awt.geom.Point2D;

//TODO: REMOVE MAX CAPACITY AND IT'S REFERENCES IN MESSAGE SYSTEM
public class RoverInfo {
    private int speed;
    private int capacity;
    private double energy;
    private double scanRange;
    private int resourceType;
    private Point2D.Double relativePosition;
    private int scannerNo;

//    public  RoverInfo(){
//
//    }

    public RoverInfo(int speed, int capacity, double energy, double scanRange, int resourceType, Point2D.Double relativePosition, int scannerNo) {
        this.speed=speed;
        this.capacity=capacity;
        this.energy=energy;
        this.scanRange=scanRange;
        this.resourceType=resourceType;
        this.relativePosition=relativePosition;
        this.scannerNo=scannerNo;
    }



    public int getSpeed() {
        return speed;
    }

    public double getScanRange() {
        return scanRange;
    }

    public int getResourceType() {
        return resourceType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public Point2D.Double getRelativePosition() {
        return relativePosition;
    }

    public void  setRelativePosition(double x, double y) {
       relativePosition.setLocation(x, y);
    }

    public void setScannerNo(int scannerNo) {
        this.scannerNo = scannerNo;
    }

    public int getScannerNo() {
        return scannerNo;
    }
}

