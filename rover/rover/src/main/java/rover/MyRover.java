package rover;

import java.awt.geom.Point2D;
import java.util.*;

import static java.lang.Math.sqrt;
//TODO: Fix record scan, sometimes when recording scan creates too many indexes
//TODO: Fix attempting to collect resource that isn't there

public abstract class MyRover extends Rover {
    public HashMap<Integer, ResourceInfo> resourceMap;  //resource location, type, amount
    public HashMap<String, RoverInfo> roverMap;         //other rovers coordinates and info, key roverID
    public RoverInfo roverInfo;                         //this rovers info
    public Boolean scanComplete;                        //whether scan is complete, used for control flow
    public int scanIndex;
    public int  toScan;                        //this needs incrementing in the scanner result, account for scenario where multiple resouces discovered in a scan
    public double scanX;
    public int noXScans;
    public double scanSquareLength;
    public boolean allCollected;
    private int currentResource;



    public MyRover() {
        super();

    }

/*    //seems OK, normal deposit method, but update rover info to show increased capacity
    public void deposit() throws Exception {
        super.deposit();
        roverInfo.setCapacity(roverInfo.getCapacity() + 1);

    }*/

    //TODO:Think about when this is called, if x or y >l/2 should be going the other way
    //calls super.move and changes the rover info relative coordinates to new coordinates
    //broadcasts rover status to team
    //looks OK, check if it gets past super.move
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
    public void recordScan(ScanItem scanItem){
        Boolean alreadyRecorded=false;
        Point2D.Double location = new Point2D.Double(scanItem.getxOffset()+roverInfo.getRelativePosition().getX()
                ,scanItem.getyOffset()+roverInfo.getRelativePosition().getY());

        if (!resourceMap.isEmpty()){
            double xDiff;
            double yDiff;
            for (int i:resourceMap.keySet()){
                xDiff  =Math.abs(location.getX()-resourceMap.get(i).getCoordinate().getX());
                yDiff=Math.abs(location.getY()-resourceMap.get(i).getCoordinate().getY());
                if (xDiff<0.1 && yDiff<0.1){
                    alreadyRecorded=true;
                }
            }
        }
        if(!alreadyRecorded){
            resourceMap.put(scanIndex, new ResourceInfo(scanItem.getResourceType(), amountFromScenario(getScenario()), location));
            // broadcastResource(location.getX(),location.getY(),scanItem.getResourceType());
            scanIndex++;
            toScan=toScan-amountFromScenario(getScenario());
            if(toScan==0){
                scanComplete=true;
            }
        }
    }

    //looks OK
    public void moveNextScanPosition() throws Exception{
        if (scanX != noXScans) {
            scanX++;
            move(scanSquareLength, 0.0, roverInfo.getSpeed());

        } else {
            scanX = 1;
            move(0, scanSquareLength, roverInfo.getSpeed());
        }
    }

    //looks OK
    public void moveClosestResource() throws Exception {
        currentResource=getClosestResource();
        Point2D.Double move=shortestXandYBetweenPoints(roverInfo.getRelativePosition(),
                resourceMap.get(currentResource).getCoordinate());

        move(move.getX(),move.getY(),roverInfo.getSpeed());
    }

    //looks OK
    public int getClosestResource(){
        int closestIndex=0;
        double closestDistance= diagonalDistance(getWorldWidth(),
                getWorldHeight()); //resource will always be closer than this

        for(int  i:resourceMap.keySet()){
            Point2D.Double resCoord=resourceMap.get(i).getCoordinate();
            Point2D.Double xyChange= shortestXandYBetweenPoints(roverInfo.getRelativePosition(), resCoord);
            double distance= diagonalDistance(xyChange.getX(), xyChange.getY());
            if (distance<closestDistance){
                closestDistance=distance;
                closestIndex=i;
            }
        }
        return closestIndex;
    }

    //looks OK
    public void returnToBase() throws Exception {
        move(-roverInfo.getRelativePosition().getX(),
                -roverInfo.getRelativePosition().getY(),
                roverInfo.getSpeed());
    }

    //Looks OK
    public double calculateScanEnergy (double range){
        return 10*(range/roverInfo.getScanRange());
        //from formula energy/second 2 * (range / maxRange) and lasts 5sec
    }

    //Looks OK
    public double calculateMoveEnergy(double x, double y){
        int timeMS = (int) (( sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / roverInfo.getSpeed()) * 1000);
        return (double) timeMS / 0.002 ; //cost is 0.002 per ms
        //from formula energy/second 2 * (speed / maxSpeed). No need to not go max speed so use 2
        //time is distance/speed
    }

    //Looks OK
    public void doNothing() throws Exception{
        move(0,0,0);
        //add field to rover info Boolean finished?
        //do nothing only called when not enough energy to be useful?
        //so broadcastRoverDepleted and remover rover from hash map?
    }

    //looks OK
    public double diagonalDistance(double x, double y){
        return sqrt(Math.pow(x,2)+Math.pow(y,2));
    }

    //looks OK
    public Point2D.Double shortestXandYBetweenPoints(Point2D.Double point1, Point2D.Double point2){
        double xChange = point2.getX() - point1.getX();
        double yChange = point2.getY() - point1.getY();
        if (xChange > getWorldWidth()/2) {
            xChange=xChange-getWorldWidth();
        }
        if (yChange > getWorldHeight()/2){
            yChange=yChange-getWorldHeight();
        }
        return new Point2D.Double(xChange,yChange);
    }

    //looks OK
    public int amountFromScenario(int scenario) { //how many stacked/scenario
        switch (scenario){
            case 0: return 10;
            case 1: return 5;
            case 2: return 5;
            case 3: return 1;
            case 4: return 1;
            case 5: return 2;
            case 6: return 1;
            case 7: return 5;
            case 8: return 15;
            case 9: return 2;
            default: return 0;

        }
    }

    public void collect() { //handle updating rover's map of resources
        try {
            super.collect();                            //collect
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!resourceMap.isEmpty()){
                for(int i:resourceMap.keySet()){
                    double xDiff=Math.abs(roverInfo.getRelativePosition().getX()-resourceMap.get(i).getCoordinate().getX());
                    double yDiff=Math.abs(roverInfo.getRelativePosition().getY()-resourceMap.get(i).getCoordinate().getY());

                    if (xDiff<0.1 && yDiff<0.1){                      //for items in res map, if same as current location
                   // System.out.println("Index: " + i);
                   // System.out.println("Amount: " + resourceMap.get(i).getAmount());
                        int amount = resourceMap.get(i).getAmount() - 1;
                       // System.out.println("Amount after collect = " + amount);
                        if (amount==0){                                     //remove from map if last resource
                          //  System.out.println("Attempting to remove");
                            resourceMap.remove(i);

                        }else{
                            resourceMap.get(i).setAmount(amount);           //else set new amount
                        }

                    }

                }
            }
        }


    }

    public Boolean resourceAtLocation(){
        if (!resourceMap.isEmpty()){
            for(int i:resourceMap.keySet()){
                double xDiff=Math.abs(roverInfo.getRelativePosition().getX()-resourceMap.get(i).getCoordinate().getX());
                double yDiff=Math.abs(roverInfo.getRelativePosition().getY()-resourceMap.get(i).getCoordinate().getY());

                if (xDiff<0.1 && yDiff<0.1){
                    return true;
                } else {
                    return  false;
                }
            }

        } else {
            return false;
        }
        return false;
    }

    public void resourcePrint(){
        for(int  i:resourceMap.keySet()){
            System.out.println("Resource Index, amount" + i + ", " + resourceMap.get(i).getAmount());
        }
    }

    void parseMessages() {
        String[] messagesArray = messages.toArray(new String[messages.size()]);        //array of all received messages

        for (int i = 0; i < messages.size(); i++) {                                    //for all messages in array
            List<String> message = Arrays.asList(messagesArray[i].split(","));  //convert message into string array on ,
            String[] messageArray = message.toArray(new String[message.size()]);

            switch (messageArray[0]) {                                                 //first string indicates message type

                //format:RESOURCE,indexInt,xDouble,yDouble,typeInt
                case "RESOURCE":
                    double resX = Double.parseDouble(messageArray[2]);
                    double resY = Double.parseDouble(messageArray[3]);
                    int resType = Integer.parseInt(messageArray[4]);
                    Point2D.Double resCoord = new Point2D.Double(resX, resY);
                    ResourceInfo resourceInfo = new ResourceInfo(resType, amountFromScenario(getScenario()), resCoord);
                    resourceMap.put(Integer.parseInt(messageArray[1]), resourceInfo);
                    break;

                //format:"ROVER",idStr,xDouble,yDouble,intSpeed,intCapacity,doubleEnergy,intScan,intType,intMaxCapacity
                case "ROVER":
                    double rovX = Double.parseDouble(messageArray[2]);
                    double rovY = Double.parseDouble(messageArray[3]);
                    Point2D.Double rovCoord = new Point2D.Double(rovX, rovY);

                    RoverInfo roverInfo = new RoverInfo(Integer.parseInt(messageArray[4]),
                            Integer.parseInt(messageArray[5]), Double.parseDouble(messageArray[6])
                            , Integer.parseInt(messageArray[7]), Integer.parseInt(messageArray[8]),
                            rovCoord,Integer.parseInt(messageArray[9]));
                    roverMap.remove(messageArray[1]);
                    roverMap.put(messageArray[1], roverInfo);
                    break;

                //format:SCANCOMPLETE
                case "SCANCOMPLETE":
                    scanComplete=true;
                    break;

                //format:COLLECTED,intNumber
                case "COLLECTED":
                    Integer resIndex =Integer.parseInt(messageArray[1]);
                    int remaining =resourceMap.get(resIndex).getAmount() - 1;
                    int type = resourceMap.get(resIndex).getType();
                    Point2D.Double location=resourceMap.get(resIndex).getCoordinate();
                    resourceMap.remove(resIndex);
                    if (remaining!=0){
                        resourceMap.put(resIndex, new ResourceInfo(type, remaining, location)); }
                    break;

                case"DEPLETED":
                    roverMap.remove(messageArray[1]);
            }

        }
        messages.clear();

    }

}

/*    //looks OK
    MAY NOT BE NEEDED
    public void collectResource(int resIndex) throws Exception {
        Point2D.Double move=shortestXandYBetweenPoints(roverInfo.getRelativePosition(),
                resourceMap.get(resIndex).getCoordinate());
        System.out.println("Collect res test " + move.getX() +" "+ move.getY());
        move(move.getX(),move.getY(), roverInfo.getSpeed());
        try {
            System.out.println("Collect res test index=" + resIndex);
            collect(resIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

//seems OK
//pass in index of resource being collected, remove if it is now empty

//MAY NOT BE NEEDED
    /*public void collect(int resIndex) throws Exception {
        Boolean collected = false;
        int currentLoad=getCurrentLoad();
        System.out.println("About to collect");
        while (!collected){
            System.out.println("trying to collect");
            try {
                super.collect();
            } catch (Exception e) {
                e.printStackTrace();
            } if (getCurrentLoad()>currentLoad){
                collected = true;
            }
        }


        System.out.println("Post collect world bby");
        System.out.println("Current load " + getCurrentLoad());
        roverInfo.setCapacity(roverInfo.getCapacity() - 1);
        if (resourceMap.get(resIndex).getAmount() - 1 == 0){        //if empty after resource collected
            resourceMap.remove(resIndex);                           //remove from resource map
        } else {
            resourceMap.get(resIndex).setAmount(resourceMap.get(resIndex).getAmount() - 1);
        }                                                           //else decrease amount by 1
        roverInfo.setEnergy(roverInfo.getEnergy() -5);
      //  broadcastCollected(resIndex);
        //broadcastStatus();
        System.out.println("END OF COLLECT REACHED");
    }*/


/*NOT NEEDED FOR INDIVIDUAL ROVER

    //format:COLLECTED,intNumber
    void broadcastCollected(int index){
        String message = "COLLECTED," +
                index ;
        broadCastToTeam(message);

    }

    //looks OK
    //BROADCAST TO TEAM DOES NOT SEND TO SELF, BEFORE THIS METHOD IS CALLED UPDATE ROVERS OWN RESOURCE/ROVERMAP
    void broadcastStatus() {
        //format"Rover,ID,xCo,yCo,Speed,Capacity,Energy,Scan,ResType
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
                roverInfo.getResourceType()+
                "," + roverInfo.getMaxCapacity();

        broadCastToTeam(message);

    }

    //Looks OK
    //format:RESOURCE,indexInt,xDouble,yDouble,typeInt
    //needs to be called every time scanned resource is incremented
    public void broadcastResource(Double xCoord, Double yCoord, int resType) {
        String message = "RESOURCE," +
                scanIndex +
                "," +
                xCoord +
                "," +
                yCoord +
                "," +
                resType;

        broadCastToTeam(message);
        //increment scanned resource used for resource index
    }

    //looks OK
    public void broadcastScanComplete(){
        broadCastToTeam("SCANCOMPLETE");
    }

    //Format:DEPLETED,strRoverID
    //looks OK
    public void broadCastDepleted(){    //takes rover out of contention
        String message = "DEPLETED," +
                getID();
        broadCastToTeam(message);
    }



 */