package rover;

public class RoverInfo {
    private int speed;
    private int capacity;
    private double energy;
    private int scanRange;
    private int resourceType;

    RoverInfo(){

    }

    public RoverInfo(int speed, int capacity, double energy, int scanRange, int resourceType) {
        this.speed=speed;
        this.capacity=capacity;
        this.energy=energy;
        this.scanRange=scanRange;
        this.resourceType=resourceType;
    }

    public int getSpeed() {
        return speed;
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

    public int getScanRange() {
        return scanRange;
    }


    public int getResourceType() {
        return resourceType;
    }

//after every action by every rover, do energy=getEnergy();

}

