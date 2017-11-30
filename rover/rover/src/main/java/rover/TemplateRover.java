package rover;

import java.awt.geom.Point2D;
import java.util.HashMap;

public class TemplateRover extends MyRover {
    private int moveState; //0= scan move, 1= move to resource, 2=finish return to base move


    public TemplateRover() {
        super();

        try {
            setAttributes(1, 5, 3, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    void begin() {
        //called when the world is started
        getLog().info("BEGIN!");
        int speed = 1;
        int capacity = 3;
        double scanRange = 5;
        int resourceType = 1;
        roverInfo = new RoverInfo(speed, capacity, getEnergy(), scanRange, resourceType, new Point2D.Double(0, 0), capacity);
        scanComplete = false;
        scanSquareLength = Math.sqrt(Math.pow(4 * scanRange, 2) / 2);
        scansPerRow = (int) Math.ceil(getWorldWidth() / scanSquareLength);
        resourceMap = new HashMap<>();
        roverMap = new HashMap<>();
        resourceIndex = 0;
        toScan = getWorldResources();
        scanXIndex = 1; //start with scan, set index to 1,1
        moveState = 0;
        allCollected=false;
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
        roverInfo.setEnergy(getEnergy()); //set energy for each poll

        switch (pr.getResultType()) {
            case PollResult.MOVE:
                switch (moveState) {
                    case 0://when move comes from scan poll result and scan not complete
                        try {
                            scan(roverInfo.getScanRange());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    //TODO: implement collect method such that resource removed from resourceMap
                    case 1://move comes from moving to resource
                        try {
                            System.out.println("Resource Index print before collect ");
                            resourcePrint();
                            collect();
                           // System.out.println("Resource Index print after collect ");
                            resourcePrint();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2://move comes from moving to base
                        try {
                            deposit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }

                break;

            case PollResult.SCAN:
                for (ScanItem item : pr.getScanItems()) {
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        recordScan(item);
                        System.out.println("Print Res Map after scan recorded");
                        resourcePrint();
                    }
                }
                getLog().info("Scan instance performed");
                if (scanComplete) {
                    getLog().info("Scan complete");
                    moveState = 1;
                    try {
                        moveClosestResource();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        moveNextScanPosition();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            //TODO: check resource removal logic from resource map, verify state logic for when resource is empty, check and verify map scan, start with initial x scan so xScan =1?
            //TODO: calculate if resource still available
            case PollResult.COLLECT:                    //do nothing after deposit, move to establish control in move case
                getLog().info("Collect complete.");
                roverInfo.setCapacity(roverInfo.getCapacity() - 1);//handle capacity here
                if (roverInfo.getCapacity() == 0 || !resourceAtLocation())  {
                    try {
                        returnToBase(); //if after collection there are no more resource, return to base
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    moveState = 2;
                } else {
                    System.out.println("MOVE STATE SET TO 2");
                    try {
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }


                break;

            case PollResult.DEPOSIT:                    //do nothing after deposit, move to establish control in move case
                getLog().info("Deposit complete.");
                //System.out.println(roverInfo.getCapacity());
                if (getCurrentLoad()!=0){
                    try {
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {            //TODO:add what to do when ended
                    moveState=1;
                    try {
                        moveClosestResource();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }

    public void resourcePrint(){
        for(int  i:resourceMap.keySet()){
            System.out.println("Resource Index, amount" + i + ", " + resourceMap.get(i).getAmount());
        }
    }
}