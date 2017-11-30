package rover;

import java.awt.geom.Point2D;
import java.util.*;

import static java.lang.Math.sqrt;

//TODO: Fix attempting to collect resource that isn't there

public abstract class MyRover extends Rover {
    HashMap<Integer, ResourceInfo> resourceMap;  //resource location, type, amount
    HashMap<String, RoverInfo> roverMap;         //other rovers coordinates and info, key roverID
    RoverInfo roverInfo;                         //this rovers info
    Boolean scanComplete;                        //whether scan is complete, used for control flow
    int resourceIndex;
    int toScan;                        //this needs incrementing in the scanner result, account for scenario where multiple resouces discovered in a scan
    double scanXIndex;
    int scansPerRow;
    double scanSquareLength;
    boolean allCollected;
    boolean negativeY;
    int scannersFinished;


    public MyRover() {
        super();
        setTeam("der26");

    }


    //calls super.move and changes the rover info relative coordinates to new coordinates
    //broadcasts rover status to team
    //looks OK, check if it gets past super.move
    @Override
    public void move(double xChange, double yChange, double speed) {
        try {
            super.move(xChange, yChange, speed);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            double newX = xChange + roverInfo.getRelativePosition().getX();
            double newY = yChange + roverInfo.getRelativePosition().getY();
            if (newX > (getWorldWidth() / 2)) {
                newX = newX - getWorldWidth();
            }
            if (newY > (getWorldHeight() / 2)) {
                newY = newY - getWorldHeight();
            }
            roverInfo.setRelativePosition(newX, newY);
            //broadcastStatus();
        }

    }

    //looks OK
    void recordScan(ScanItem scanItem) {
        Boolean alreadyRecorded = false;
        Point2D.Double location = new Point2D.Double(scanItem.getxOffset() + roverInfo.getRelativePosition().getX()
                , scanItem.getyOffset() + roverInfo.getRelativePosition().getY());
        double newX = location.getX();
        double newY = location.getY();
        if (newX > (getWorldWidth() / 2)) {             //if new distance is greater than w/2 from origin, set to other side of map
            newX = newX - getWorldWidth();
        }
        if (newY > (getWorldHeight() / 2)) {
            newY = newY - getWorldHeight();
        }
        location.setLocation(newX, newY);

        if (!resourceMap.isEmpty()) {   //check if item already recoreded
            double xDiff;
            double yDiff;
            for (int i : resourceMap.keySet()) {
                xDiff = Math.abs(location.getX() - resourceMap.get(i).getCoordinate().getX());
                yDiff = Math.abs(location.getY() - resourceMap.get(i).getCoordinate().getY());
                if (xDiff <= 0.1 && yDiff <= 0.1) {   //already recorded if coord within 0.1
                    alreadyRecorded = true;
                }
            }
        }
        if (!alreadyRecorded) {     //if not already recorded, put in map
            resourceMap.put(resourceIndex, new ResourceInfo(scanItem.getResourceType(), amountFromScenario(getScenario()), location));
            // broadcastResource(location.getX(),location.getY(),scanItem.getResourceType());
            resourceIndex++;
            toScan = toScan - amountFromScenario(getScenario()); //take resources away from resources left to scan
            if (toScan == 0) {                                  //no more resources to find, scan complete
                scanComplete = true;                            //return to pr result scan
            }
        }
    }


    void moveNextScanPosition() throws Exception {
        if (scanXIndex < scansPerRow) {
            scanXIndex++;
            move(scanSquareLength, 0.0, roverInfo.getSpeed());

        } else {
            scanXIndex = 1;
            if (negativeY) {
                move(0, -scanSquareLength, roverInfo.getSpeed());
            } else {
                move(0, scanSquareLength, roverInfo.getSpeed());
            }
        }
    }


    //TODO:CHECK
    //given two points, check which way round map is quicker for both
    //i.e if 20x20 coords -10 to 10
    private Point2D.Double shortestRoute(Point2D.Double start, Point2D.Double end) {
        double xChange = end.getX() - start.getX();
        double yChange = end.getY() - start.getY();
        if (xChange > getWorldWidth() / 2) {
            xChange = xChange - getWorldWidth();
        }
        if (yChange > getWorldHeight() / 2) {
            yChange = yChange - getWorldHeight();
        }
        return new Point2D.Double(xChange, yChange);
    }

    //looks OK
    void moveClosestResource() throws Exception {
        int currentResource = getClosestResource();
        Point2D.Double move = shortestRoute(roverInfo.getRelativePosition(),
                resourceMap.get(currentResource).getCoordinate());

        move(move.getX(), move.getY(), roverInfo.getSpeed());
    }

    //looks OK
    private int getClosestResource() {
        int closestIndex = 0;
        double closestDistance = getWorldWidth() * getWorldHeight(); //resource will always be closer than this

        for (int i : resourceMap.keySet()) {
            Point2D.Double resCoord = resourceMap.get(i).getCoordinate(); //coordinate of resource
            Point2D.Double shortestRoute = shortestRoute(roverInfo.getRelativePosition(), resCoord); //x movement and y movement for shortest
            double distance = diagonalDistance(resCoord, shortestRoute);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    //looks OK
    void returnToBase() throws Exception {
        move(-roverInfo.getRelativePosition().getX(),
                -roverInfo.getRelativePosition().getY(),
                roverInfo.getSpeed());
    }

    //Looks OK
    double calculateScanEnergy(double range) {
        return 10 * (range / roverInfo.getScanRange());
        //from formula energy/second 2 * (range / maxRange) and lasts 5sec
    }

    //Looks OK
    double calculateMoveEnergyPoints(Point2D.Double x, Point2D.Double y) {
        return 2 * diagonalDistance(x, y); //cost is 0.002 per ms
        //from formula energy/second 2 * (speed / maxSpeed). No need to not go max speed so use 2
        //time is distance/speed
    }

    //looks OK
    double calculateMoveEnergyLength(double distance) {
        return 2 * distance; //cost is 0.002 per ms
        //from formula energy/second 2 * (speed / maxSpeed). No need to not go max speed so use 2
        //time is distance/speed
    }


    //looks OK
    private double diagonalDistance(Point2D.Double start, Point2D.Double end) {
        double xChange = end.getX() - start.getX();
        double yChange = end.getY() - start.getY();
        return sqrt(Math.pow(xChange, 2) + Math.pow(yChange, 2));
    }

    //looks OK
    private int amountFromScenario(int scenario) { //how many stacked/scenario
        switch (scenario) {
            case 0:
                return 10;
            case 1:
                return 5;
            case 2:
                return 5;
            case 3:
                return 1;
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 1;
            case 7:
                return 5;
            case 8:
                return 15;
            case 9:
                return 2;
            default:
                return 0;

        }
    }

    //TODO:Update so that if res coord > worldWidth/2 put other side of map
    @Override
    public void collect() { //handle updating rover's map of resources
        try {
            super.collect();                            //collect
        } catch (Exception e) {
            move(0, 0, roverInfo.getSpeed());
            e.printStackTrace();
        } finally {

            if (!resourceMap.isEmpty()) {
                for (int i : resourceMap.keySet()) {
                    double xDiff = Math.abs(roverInfo.getRelativePosition().getX() - resourceMap.get(i).getCoordinate().getX());
                    double yDiff = Math.abs(roverInfo.getRelativePosition().getY() - resourceMap.get(i).getCoordinate().getY());

                    if (xDiff <= 0.1 && yDiff <= 0.1) {                      //for items in res map, if same as current location
                        System.out.println("Index: " + i);
                        System.out.println("Amount: " + resourceMap.get(i).getAmount());
                        int amount = resourceMap.get(i).getAmount() - 1;
                        System.out.println("Amount after collect = " + amount);
                        if (amount == 0) {                                     //remove from map if last resource
                            System.out.println("Attempting to remove");
                            resourceMap.remove(i);

                        } else {
                            resourceMap.get(i).setAmount(amount);           //else set new amount
                        }

                    }

                }
            }
        }


    }


    Boolean resourceAtLocation() {
        if (!(resourceMap.isEmpty())) {
            for (int i : resourceMap.keySet()) {
                double xDiff = Math.abs(roverInfo.getRelativePosition().getX() - resourceMap.get(i).getCoordinate().getX());
                double yDiff = Math.abs(roverInfo.getRelativePosition().getY() - resourceMap.get(i).getCoordinate().getY());
                if (xDiff <= 0.1 && yDiff <= 0.1) return true;
            }

        } else {
            return false;
        }
        return false;
    }

    public void resourcePrint() {
        System.out.println("Resource Index, amount, location: ");
        System.out.println("World Width X Height: " + getWorldWidth()+" X "+ getWorldHeight());
        for (int i : resourceMap.keySet()) {
            System.out.println( i + ", " + resourceMap.get(i).getAmount()+resourceMap.get(i).getCoordinate());
        }
    }

    void parseMessages() {
        super.retrieveMessages();
        String[] messagesArray = messages.toArray(new String[messages.size()]);
        for (String messageString : messagesArray) {
            List<String> message = Arrays.asList(messageString.split(","));
            String[] messageArray = message.toArray(new String[message.size()]);

            switch (messageArray[0]) {                                                 //first string indicates message type

                //Only ever seen by scanner1?
                //format:RESOURCE,indexInt,xDouble,yDouble,typeInt
                case "RESOURCE":
                    Boolean alreadyRecorded = false;

                    double resX = Double.parseDouble(messageArray[2]);
                    double resY = Double.parseDouble(messageArray[3]);
                    int resType = Integer.parseInt(messageArray[4]);
                    Point2D.Double resCoord = new Point2D.Double(resX, resY);
                    ResourceInfo resourceInfo = new ResourceInfo(resType, amountFromScenario(getScenario()), resCoord);

                    for (int i : resourceMap.keySet()) {
                        double xDiff = Math.abs(resX - resourceMap.get(i).getCoordinate().getX());
                        double yDiff = Math.abs(resY - resourceMap.get(i).getCoordinate().getY());
                        if (xDiff <= 0.1 && yDiff <= 0.1) {
                            alreadyRecorded = true;
                        }
                    }

                    if (!alreadyRecorded) {
                        resourceMap.put(resourceIndex, resourceInfo);
                        resourceIndex++;
                    }
                    break;


                //format:"ROVER",idStr,xDouble,yDouble,intSpeed,intCapacity,doubleEnergy,doubleScan,intType,intScannerNo
                case "ROVER":
                    String id = messageArray[1];
                    double rovX = Double.parseDouble(messageArray[2]);
                    double rovY = Double.parseDouble(messageArray[3]);
                    Point2D.Double rovCoord = new Point2D.Double(rovX, rovY);
                    int speed = Integer.parseInt(messageArray[4]);
                    int capacity = Integer.parseInt(messageArray[5]);
                    double energy = Double.parseDouble(messageArray[6]);
                    double scan = Double.parseDouble(messageArray[7]);
                    int type = Integer.parseInt(messageArray[8]);
                    int scannerNo = Integer.parseInt(messageArray[9]);

                    RoverInfo roverInfo = new RoverInfo(speed, capacity, energy, scan, type, rovCoord, scannerNo);
                    roverMap.remove(id);
                    roverMap.put(id, roverInfo);
                    break;

                //only ever seen by scanner 1?
                //format:SCANCOMPLETE
                case "SCANCOMPLETE":
                    scannersFinished++;
                    break;

                //format:COLLECTED,intNumber
                case "COLLECTED":
                    Integer resIndex = Integer.parseInt(messageArray[1]);
                    int remaining = resourceMap.get(resIndex).getAmount() - 1;
                    int resTypeC = resourceMap.get(resIndex).getType();
                    Point2D.Double location = resourceMap.get(resIndex).getCoordinate();
                    resourceMap.remove(resIndex);
                    if (remaining != 0) {
                        resourceMap.put(resIndex, new ResourceInfo(resTypeC, remaining, location));
                    }
                    break;

                case "DEPLETED":
                    roverMap.remove(messageArray[1]);
            }

        }
        messages.clear(); //clear myMessages to avoid repeat processing


    }


    //looks OK
    //BROADCAST TO TEAM DOES NOT SEND TO SELF, BEFORE THIS METHOD IS CALLED UPDATE ROVERS OWN RESOURCE/ROVERMAP
    void broadcastStatus() {
        //format"Rover,ID,xCo,yCo,Speed,Capacity,Energy,Scan,ResType,ScannerNo
        String message = "ROVER," +
                getID() +
                "," +
                roverInfo.getRelativePosition().getX() +
                "," +
                roverInfo.getRelativePosition().getY() +
                "," +
                roverInfo.getSpeed() +
                "," +
                roverInfo.getCapacity() +
                "," +
                getEnergy() +
                "," +
                roverInfo.getScanRange() +
                "," +
                roverInfo.getResourceType() +
                "," +
                roverInfo.getScannerNo();
        broadCastToTeam(message);
        System.out.println("Rover ID: " + getID() + "Message broadcasted");
    }

}
