package rover;

public abstract class CarryRover extends MyRover {

    public CarryRover(){
        super();
    }

    void collectDecision(){
        //calc if enough energy to get to base with resource and another resource
        //if this rover is closest to resource, go collect it
        //if this rover has the capacity
        if (roverInfo.getCapacity() ==0){return;}

    }


}
