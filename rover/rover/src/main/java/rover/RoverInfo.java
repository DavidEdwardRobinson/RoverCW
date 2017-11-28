package rover;

import java.awt.geom.Point2D;

public class RoverInfo {
    private int speed;
    private final int maxCapacity;
    private int capacity;
    private double energy;
    private double scanRange;
    private int resourceType;
    private Point2D.Double relativePosition;

//    public  RoverInfo(){
//
//    }

    public RoverInfo(int speed, int capacity, double energy, double scanRange, int resourceType, Point2D.Double relativePosition, int maxCapacity) {
        this.maxCapacity=maxCapacity;
        this.speed=speed;
        this.capacity=capacity;
        this.energy=energy;
        this.scanRange=scanRange;
        this.resourceType=resourceType;
        this.relativePosition=relativePosition;
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

    public int getMaxCapacity(){
        return maxCapacity;
    }


}

