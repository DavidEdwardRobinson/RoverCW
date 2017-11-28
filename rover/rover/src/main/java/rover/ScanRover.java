package rover;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Random;
/*
* Scan x has r=2x
* To scan the whole map take the biggest square in the circle
* d^2=2length^2 length=root(d^2/2)
* total scans needed = worldW*worldH/length^2 rounded up (maybe not assumes fits perfectly?)
* conservative scans needed: scan move right by length scan until relative pos + 2*length [0,0]
* move up by length, repeat above step
* continue until relative pos + 2length [0,0]
* broadcast scan complete
*
* Hardcode the distribution of resources, i.e
*
* Get scan rover to distribute the work to the other rovers at the end with SCANCOMPLTE broadcast?
* i.e SCANCOMPLETE
* split the map into equally weighted parts so collect rovers 1 and collect rovers2 all collect same amount
* ROVERCOMMAND,
* */


//WHEN SCANNING, DON'T ATTEMPT TO BROADCAST LOCATIONS ALREADY FOUND

public class

ScanRover extends MyRover {

    public ScanRover() {
        super();
        try {
            //set attributes for this rover
            //speed, scan range, max load
            //has to add up to <= 9
            //Fourth attribute is the collector type
            setAttributes(1, 8, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    void begin() {
        getLog().info("BEGIN!");
        int speed = 1;
        int capacity = 0;
        double scanRange = 8;
        int resourceType = 1;
        roverInfo = new RoverInfo(speed, capacity, getEnergy(), scanRange, resourceType, new Point2D.Double(0, 0), capacity);
        scanComplete = false;
        scanSquareLength = Math.sqrt(Math.pow(4 * scanRange, 2) / 2);
        noXScans = (int) Math.ceil(getWorldWidth() / scanSquareLength);
        resourceMap = new HashMap<>();
        roverMap = new HashMap<>();
        scanIndex = 0;
        toScan = getWorldResources();
        scanX = 1; //start with scan, set index to 1,1

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
        resourcePrint();

        switch (pr.getResultType()) {
            case PollResult.MOVE:          //when finished a move, scan if scan not complete
                if(!scanComplete){
                    try {
                        scan(roverInfo.getScanRange());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else{
                    try {
                        //broadcast Scan complete
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;

            case PollResult.SCAN:           //when finished a scan, move to next scan position, and store result

                for (ScanItem item : pr.getScanItems()) {
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        recordScan(item);
                    }
                }
                getLog().info("Scan instance recorded");
                if(!scanComplete) {
                    try {
                        moveNextScanPosition();
                    } catch (Exception e) {
                        e.printStackTrace();
                        }
                }else {
                    try {
                        //broadcast Scan complete
                        move(0, 0, roverInfo.getSpeed()); //restablish move control
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }



                break;

            case PollResult.COLLECT:                    //should never have a collect pr, if it does, send back to move
                try {
                    move(0, 0, roverInfo.getSpeed()); //restablish move control
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case PollResult.DEPOSIT:                    //should never have a deposit pr, if it does, send back to move
                try {
                    move(0, 0, roverInfo.getSpeed()); //restablish move control
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;

        }
    }

    public void scanComplete(){
        broadCastToTeam("SCANCOMPLETE");
    }

    public void delegateMap(){

    }






}
