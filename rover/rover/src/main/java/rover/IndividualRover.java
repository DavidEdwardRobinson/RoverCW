package rover;

import java.awt.geom.Point2D;
import java.util.HashMap;
//TODO: SEEMS TO BE WORKING NOW, BUT CLOSEST RESOURCE DOESN'T SEEM TO WORK ANYMORE
//TODO: COULD BE AN ISSUE WITH EITHER THE WAY SCANS ARE RECORDED OR FUNCTION
//TODO: FIX SOMETIMES TIES TO COLLECT RESOURCE THAT DOESN'T EXIST
//TODO: DIDN'T MOVE CLOSEST RESOURCE? SCAN NOT RECORDED CORRECTLY
//TODO: FIX STUCK IN RESOURCE PRINT LOOP


public class IndividualRover extends MyRover {
    private int moveState; //0= scan move, 1= move to resource, 2=finish return to base move


    public IndividualRover() {
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
        System.out.println("RELOAD TEST 2");
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
        allCollected = false;
        negativeY = false;
        //Information needed to initialise rover, would have liked to have put this in the
        //constructor, but for some reason it then doesn't work with no useful debug output
        try {
            //move somewhere initially

            scan(scanRange);
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

                    case 1://move comes from moving to resource
                        try {
                             System.out.println("Resource Index print before collect ");
                            if (resourceAtLocation()){
                                collect();
                            } else {
                                moveClosestResource(); //STUCK IN LOOP HERE?
                            }

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


            //Entry point
            case PollResult.SCAN:
                for (ScanItem item : pr.getScanItems()) {
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        recordScan(item);
                        //  System.out.println("Print Res Map after scan recorded");
                        resourcePrint();
                    }
                }
                getLog().info("Scan instance performed");
                if (scanComplete) {    //when scan complete move into collect state
                    getLog().info("Scan complete");
                    moveState = 1;
                    try {
                        moveClosestResource();  //so move to closest resource
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        moveNextScanPosition();  //else next scan position
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;                            //return to pr result move
            //TODO: check resource removal logic from resource map,
            //TODO: calculate if resource still available
            case PollResult.COLLECT:                    //do nothing after collect, move to establish control in move case
                getLog().info("Collect complete.");
                //handle capacity here
                roverInfo.setCapacity(roverInfo.getCapacity() - 1);
                if (roverInfo.getCapacity() != 0 && !resourceMap.isEmpty()) { //if rover has capacity and resource left
                    try {
                        moveClosestResource();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else /*if (roverInfo.getCapacity()==0 || resourceMap.isEmpty())*/ {
                    try {
                        returnToBase();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        moveState = 2;
                    }
                }

                break;

            case PollResult.DEPOSIT:                    //do nothing after deposit, move to establish control in move case
                roverInfo.setCapacity(roverInfo.getCapacity() + 1);
                if (getCurrentLoad() != 0) {
                    try {
                        move(0, 0, roverInfo.getSpeed()); //reestablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!resourceMap.isEmpty()) {
                    moveState = 1;
                    try {
                        moveClosestResource();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

        }
    }

}