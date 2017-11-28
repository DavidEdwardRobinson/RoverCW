package rover;

import java.awt.geom.Point2D;
import java.util.HashMap;

public abstract class CarryRover extends MyRover {
    private int moveState;

    public CarryRover(){
        super();
    }

    //TODO: ADD ENERGY CONSIDERATION AND ROVER COMMUNICATION
    @Override
    void begin() {
        //called when the world is started
        getLog().info("BEGIN!");
        int speed = 1;
        int capacity = 8;
        double scanRange = 0;
        int resourceType = 1;
        roverInfo = new RoverInfo(speed, capacity, getEnergy(), scanRange, resourceType, new Point2D.Double(0, 0), capacity);
        scanComplete = false;
        resourceMap = new HashMap<>();
        roverMap = new HashMap<>();
        moveState = 0;  //waiting, initial move to resource, collecting, depositing 0-3
        //Information needed to initialise rover, would have liked to have put this in the
        //constructor, but for some reason it then doesn't work with no useful debug output
        try {
            move(0,0,roverInfo.getSpeed());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    void end() {
        // called when the world is stopped
        // the agent is killed after this
        getLog().info("END!");
    }

    //TODO: Add energy considerations, fix issue where resource amount is +1
    @Override
    void poll(PollResult pr) {
        getLog().info("Remaining Power: " + getEnergy());
        if (pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
            return;
        }
        parseMessages();                    //updates rovers knowledge of world
        roverInfo.setEnergy(getEnergy()); //set energy for each poll
        if (scanComplete){moveState=1;}

        switch (pr.getResultType()) {
            case PollResult.MOVE:           //what to do after move
                switch (moveState) {
                    //case 0 means scan is not complete, wait for scan to complete
                    case 0:
                        try {
                           move(0,0,roverInfo.getSpeed());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    //case 1 means scan has just completed, move to closest resource
                    //SHOULD ONLY BE IN MOVE STATE 1 FOR 1 ITERATION
                    case 1:
                        try {
                            moveClosestResource();
                        } catch (Exception e){
                            e.printStackTrace();
                        } finally {
                            moveState=2;
                        }
                        break;       //transition to move state 2, from here flip between 2 and 3 to collect resources

                    case 2://move comes from moving to resource, only move to state 2 if rover can collect
                        try {
                            collect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 3://move comes from moving to base
                        try {
                            deposit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }

                break;

            case PollResult.SCAN: //should never occur
                try {
                    move(0,0,roverInfo.getSpeed());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case PollResult.COLLECT:                    //what to do after collect success
                roverInfo.setCapacity(roverInfo.getCapacity() - 1);//handle capacity here

                if (roverInfo.getCapacity()!=0 && !resourceMap.isEmpty()){ //if rover has capacity and resmap not empty
                    try {
                        moveClosestResource();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else /*if (roverInfo.getCapacity()==0 || resourceMap.isEmpty())*/{
                    try {
                        returnToBase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        moveState=3;
                    }
                }

                break;

            case PollResult.DEPOSIT:                    //do nothing after deposit, move to establish control in move case
                roverInfo.setCapacity(roverInfo.getCapacity()+1);
                if (getCurrentLoad() != 0) {
                    try {
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!resourceMap.isEmpty()){            //TODO:add what to do when ended
                    moveState = 2;
                    try {
                        moveClosestResource();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {  //no resources to deposit or collect, return to idle
                    moveState=0;
                    try {
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }


}
