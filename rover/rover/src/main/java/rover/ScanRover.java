package rover;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

//TODO: verify start calculation for scanners>2


public class

ScanRover extends MyRover {
   private int scannerNo;
   private Point2D.Double startPosition;
   private int noScans;
   private int scanState; //0 is initialising scanners, 1 moving to scan position, 2 scanning, 3 distributing to carry rovers
    private Boolean completionBroadcast;
   private Boolean workSent;


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
        scanComplete = false;
        scanSquareLength = Math.sqrt(Math.pow(4 * scanRange, 2) / 2);
        scansPerRow = (int) Math.ceil(getWorldWidth() / scanSquareLength);
        resourceMap = new HashMap<>();
        roverMap = new HashMap<>();
        resourceIndex = 0;
        toScan = getWorldResources();
        scanXIndex = 1; //start with scan, set index to 1,1
        scannerNo = ThreadLocalRandom.current().nextInt(1, getScannerFromScenario(getScenario()) + 1);
        startPosition = new Point2D.Double(0, 0);
        roverInfo = new RoverInfo(speed, capacity, getEnergy(), scanRange, resourceType,
                new Point2D.Double(0, 0), scannerNo);
        System.out.println("Rover ID: " + getID() + "About to broadcast status");
        broadcastStatus();
        scanState = 0;
        scannersFinished = 0;
        completionBroadcast = false;
        workSent = false;
        scannersFinished = 0;
        try {
            move(0, 0, roverInfo.getSpeed());
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


    /*
    states:
    0=Scanners not yet initialised
    1=Scanning move/scan loop
    2=finished/delegating work
     */
    @Override
    void poll(PollResult pr) {
        getLog().info("Remaining Power: " + getEnergy());
        if (pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
            return;
        }

        roverInfo.setEnergy(getEnergy());       //set energy for each poll
        try {
            parseMessages();                        //parse messages to update rovers knowledge
        } catch (Exception e) {
            System.out.println("Rover ID: " + getID() + "parseThrewException");
            e.printStackTrace();
        }

        switch (pr.getResultType()) {
            case PollResult.MOVE:

                //initial state is 0
                //keep chaning scanNo randomly until all are unique
                //when they are unique, calc and move to start position
                //change state to 1 to signal scan
                switch (scanState) {
                    case 0:
                        if (scannersUnique()) {
                            calculateScanStartPos();
                            scanState = 1;
                            move(startPosition.getX(), startPosition.getY(), roverInfo.getSpeed());

                        } else {
                            System.out.println("Rover ID: " + getID() + " Scanners not unique");
                            reAssignScanner();
                            broadcastStatus();
                            move(0, 0, roverInfo.getSpeed());
                        }
                        break;

                    case 1:
                        try {
                            scan(roverInfo.getScanRange());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case 2:
                        if (scannerNo != 1 && !completionBroadcast) { //if not scanner1 and not already broadcast end info
                            scanComplete();                             // send out complete broadcast
                        }

                        if (scannerNo == 1 && allScannersFinished() && !workSent) {
                            resourcePrint();
                            //TODO:send work to rovers
                        }
                        try {
                            move(0, 0, roverInfo.getSpeed()); //restablish move control
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                break;


            //After every scan result, decrease number of scans
            //record scan in resource map
            //if no more scans to perform, move to state 2, delegate resources to carry rovers
            case PollResult.SCAN:
                noScans--;
                for (ScanItem item : pr.getScanItems()) {
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        recordScan(item);
                    }
                }

                if (noScans == 0) {
                    scanComplete = true;
                    scanState = 2;
                    move(0, 0, roverInfo.getSpeed());
                } else {
                    try {
                        moveNextScanPosition();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;


            case PollResult.COLLECT:                    //should never have a collect pr, if it does, send back to move
                try {
                    move(0, 0, roverInfo.getSpeed()); //restablish move control
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case PollResult.DEPOSIT:                    //should never have a deposit pr, if it does, send back to move
                try {
                    move(0, 0, roverInfo.getSpeed()); //restablish move control
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
//    //for 1 collect rover
//    private void distributeWork() {
//        for (int i:resourceMap.keySet()){
//            br
//        }
//    }


    private boolean allScannersFinished() {
        return scannersFinished == getScannerFromScenario(getScenario()) - 1;
    }

    private void scanComplete() {
        String Scanner1ID = null;
        for (String ID : roverMap.keySet()) {
            if (roverMap.get(ID).getScannerNo() == 1) {
                Scanner1ID = ID;
            }
        }

        for (int i : resourceMap.keySet()) {
            String resMessage = "RESOURCE," + 1 + "," +
                    resourceMap.get(i).getCoordinate().getX() + "," +
                    resourceMap.get(i).getCoordinate().getY() + "," +
                    resourceMap.get(i).getType();
            broadCastToUnit(Scanner1ID, resMessage);
        }
        broadCastToUnit(Scanner1ID, "SCANCOMPLETE");
        completionBroadcast = true;
    }

    //seems to work
    private void reAssignScanner() {
        System.out.println("Rover ID: " + getID() + "Reassign scanner reached");
        scannerNo = ThreadLocalRandom.current().nextInt(1, getScannerFromScenario(getScenario()) + 1);
        roverInfo.setScannerNo(scannerNo);
        broadcastStatus();
    }

    //seems to work
    private boolean scannersUnique() {
        boolean duplicates = false;
        int scannersInScenario = getScannerFromScenario(getScenario());
        int[] scannerNumbers = new int[scannersInScenario];
        scannerNumbers[0] = scannerNo;
        int i = 1;
        for (String id : roverMap.keySet()) {
            scannerNumbers[i] = roverMap.get(id).getScannerNo();
            i++;
        }

        if (roverMap.size() == scannersInScenario - 1) {
            for (i = 0; i < scannersInScenario; i++) {
                for (int j = 0; j < scannersInScenario; j++) {
                    if (i != j) {
                        if (scannerNumbers[i] == scannerNumbers[j]) {
                            duplicates = true;
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return !duplicates;
    }


    //Description:
    //Method calculates the start position of the scanner given an index and the number of scans it will
    //be able to perform based on the energy it took to get to that start position. Start position should be
    //the end position of the previous scanner
    //Assumes scanners are numbered 1-N and have unique index
    //Sends scanners with odd numbers down, even up
    //TODO: NEEDS SERIOUS SOBER TESTING
    //LOOKS OK TBF
    //currently will scan whole row again for next scanner
    //set noXscans depending on where previous scan finished
    //Energy calculation wrong
    //moves initial rover diagonally by scan square length, should just be vertical
    private void calculateScanStartPos() {
        negativeY = (scannerNo % 2 == 0);          //odd Scanners go up, even go down
        int verticalScanNo = (int) Math.ceil(scannerNo / 2);
        scansPerRow = (int) Math.ceil(getWorldWidth() / scanSquareLength);//min number of scans to cover world width also used as index for move next scan


        double colEnergy = calculateScanEnergy(roverInfo.getScanRange()) + calculateMoveEnergyLength(scanSquareLength);
        double rowEnergy = colEnergy * scansPerRow;//energy to move+scan multiply by number of rows
        double previousEndX = -scanSquareLength;
        double previousEndY = 0;
        double energy = roverInfo.getEnergy();
        System.out.println("Rover ID: " + getID() + " World width: " + getWorldWidth());
        System.out.println("Rover ID: " + getID() + " Scan square length: " + scanSquareLength);
        System.out.println("Rover ID: " + getID() + " Scans per row: " + scansPerRow);
        System.out.println("Rover ID: " + getID() + " Rover energy " + getEnergy() + " Expected: 45.5294117647)");
        System.out.println("Rover ID: " + getID() + " Energy per scan: " + calculateScanEnergy(roverInfo.getScanRange()));
        System.out.println("Rover ID: " + getID() + " Energy per move: " + calculateMoveEnergyLength(scanSquareLength));
        System.out.println("Rover ID: " + getID() + " Energy per move & scan: " + colEnergy);
        System.out.println("Rover ID: " + getID() + " Energy for whole row scan: " + rowEnergy);
        System.out.println("Rover ID: " + getID() + " Vertical Scan No: " + verticalScanNo);


        int completedRows;
        int completedCols = 0;
        Point2D.Double previousEnd = new Point2D.Double(previousEndX, previousEndY);

        //do n-1 times or n?
        for (int i = 1; i < verticalScanNo; i++) { //for scanners 1-n assigned + or - y
            energy = energy - calculateMoveEnergyPoints(previousEnd, roverInfo.getRelativePosition()); //energy is initial-energy to start postion
            completedRows = (int) Math.floor(energy / rowEnergy);                                       //how many complete rows can be scanned
            completedCols = (int) Math.floor(energy / colEnergy);                                       //how many complete cols can be scanned
            previousEndY = previousEndY + (completedRows * scanSquareLength);                           //so scan i Y ends at last end position + rows completed
            previousEndX = previousEndX + (completedCols * scanSquareLength);                           //so scan i X ends at last end position + cols completed
            previousEnd = new Point2D.Double(previousEndX, previousEndY);                                //record as coordinate
            energy = roverInfo.getEnergy();                                                             //reset energy for next rover
        }
        scanXIndex = completedCols; //MAYBE +1?

        if (negativeY) {
            startPosition.setLocation(previousEndX + scanSquareLength, -(previousEndY + scanSquareLength));
        } else {
            startPosition.setLocation(previousEndX + scanSquareLength, previousEndY + scanSquareLength);
        }
        noScans = (int) Math.floor(energy / colEnergy);
        System.out.println("Rover ID: " + getID() + "Start position: " + startPosition.getX() + "," + startPosition.getY());
        System.out.println("Rover ID: " + getID() + "Number of scans to perform: " + noScans);

    }

    private int getScannerFromScenario(int scenario) { //how many scanners in the scenario
        switch (scenario) {
            case 2:
                return 2;
            case 3:
                return 2;
            case 4:
                return 8;
            case 6:
                return 2;
            case 7:
                return 8;
            case 8:
                return 8;
            default:
                return 1;

        }
    }


}
