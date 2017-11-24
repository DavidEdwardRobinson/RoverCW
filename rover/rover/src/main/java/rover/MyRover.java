package rover;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class MyRover extends Rover {
    public HashMap<Integer, ResourceInfo> resourceMap;  //resource location, type, amount
    public HashMap<String, RoverInfo> roverMap;         //other rovers coordinates and info
    public RoverInfo roverInfo;                         //this rovers info
    public Boolean scanComplete;                        //whether scan is complete, used for control flow
    public int scannedResources;                        //this needs incrementing in the scanner result, account for scenario where multiple resouces discovered in a scan

    public MyRover() {
        super();
        resourceMap = new HashMap();
        roverMap = new HashMap();
        roverInfo=new RoverInfo();
        setTeam("der26");
        scannedResources=0;
    }

    //from formula energy/second 2 * (range / maxRange) and lasts 5sec
    public double calculateScanEnergy (double range){
        return 10*(range/roverInfo.getScanRange());
    }

    //from formula energy/second 2 * (speed / maxSpeed). No need to not go max speed so use 2
    //time is distance/speed
    public double calculateMoveEnergy(double x, double y){
        int timeMS = (int) (( Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / roverInfo.getSpeed()) * 1000);
        return (double) timeMS / 0.002 ; //cost is 0.002 per ms
    }

    //TODO: Capture poll result and broadcastResource, as well as updating this.roverInfo
    public void scan (double range) throws Exception {
        super.scan(range);                              //use Rover scan method
        roverInfo.setEnergy(roverInfo.getEnergy() - calculateScanEnergy(range));
        broadcastStatus();                                 //update energy used and broadcast status to team
    }

    //TODO:Think about when this is called, if x or y >l/2 should be going the other way
    //calls super.move and changes the rover info relative coordinates to new coordinates
    //when calling this function, should NEVER have x or y > w/2 or h/2 respectively
    public void move(double xChange, double yChange, double speed) throws Exception {
        super.move(xChange, yChange, speed);
        double newX = xChange + roverInfo.getRelativePosition().getX();
        double newY = yChange + roverInfo.getRelativePosition().getY();
        if (newX > (getWorldWidth() / 2)) {
            newX = newX - getWorldWidth();
        }
        if (newY > (getWorldHeight() / 2)) {
            newY = newY - getWorldHeight();
        }
        roverInfo.setRelativePosition(newX, newY);
        roverInfo.setEnergy(roverInfo.getEnergy() - calculateMoveEnergy(xChange,yChange));
        broadcastStatus();
    }

    //seems OK
    //pass in index of resource being collected, remove if it is now empty
    public void collect(int resIndex) throws Exception {
        super.collect();
        roverInfo.setCapacity(roverInfo.getCapacity() - 1);
        if (resourceMap.get(resIndex).getAmount() - 1 == 0){        //if empty after resource collected
            resourceMap.remove(resIndex);                           //remove from resource map
        } else {
            resourceMap.get(resIndex).setAmount(resourceMap.get(resIndex).getAmount() - 1);
        }                                                           //else decrease amount by 1
        roverInfo.setEnergy(roverInfo.getEnergy() -5);
        broadcastCollected(resIndex);
        broadcastStatus();
    }

    //seems OK, normal deposit method, but update rover info to show increased capacity
    public void deposit() throws Exception {
        super.deposit();
        roverInfo.setCapacity(roverInfo.getCapacity() + 1);
        roverInfo.setEnergy(roverInfo.getEnergy() -5);
        broadcastStatus();

    }

    private int amountFromScenario(int scenario) { //how many stacked/scenario
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

    //format:COLLECTED,intNumber,xDouble,yDouble,intAmount
    void broadcastCollected(int index){
        String message = "COLLECTED," +
                index +
                "," +
                resourceMap.get(index).getCoordinate().getX() +
                "," +
                resourceMap.get(index).getCoordinate().getY() +
                "," +
                1;
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
                roverInfo.getResourceType();

        broadCastToTeam(message);

    }

    //Looks OK
    //format:RESOURCE,indexInt,xDouble,yDouble,typeInt
    public void broadcastResource(Double xCoord, Double yCoord, int resType) {
        String message = "RESOURCE," +
                scannedResources +
                "," +
                xCoord +
                "," +
                yCoord +
                "," +
                resType;

        broadCastToTeam(message);
        //increment scanned resource used for resource index
        scannedResources ++;

    }

    void parseMessages() {
        String[] messagesArray = messages.toArray(new String[messages.size()]);        //array of all received messages

        for (int i = 0; i < messages.size(); i++) {                                    //for all messages in array
            List<String> message = Arrays.asList(messagesArray[i].split(","));  //convert message into string array on ,
            String[] messageArray = message.toArray(new String[message.size()]);

            switch (messageArray[0]) {                                                 //first string indicates message typ

                //format:RESOURCE,indexInt,xDouble,yDouble,typeInt
                case "RESOURCE":
                    double resX = Double.parseDouble(messageArray[2]);
                    double resY = Double.parseDouble(messageArray[3]);
                    int resType = Integer.parseInt(messageArray[4]);
                    Point2D.Double resCoord = new Point2D.Double(resX, resY);
                    ResourceInfo resourceInfo = new ResourceInfo(resType, amountFromScenario(getScenario()), resCoord);
                    resourceMap.put(Integer.parseInt(messageArray[1]), resourceInfo);
                    break;

                //format:"ROVER",idStr,xDouble,yDouble,intSpeed,intCapacity,doubleEnergy,intScan,intType
                case "ROVER":
                    double rovX = Double.parseDouble(messageArray[2]);
                    double rovY = Double.parseDouble(messageArray[3]);
                    Point2D.Double rovCoord = new Point2D.Double(rovX, rovY);

                    RoverInfo roverInfo = new RoverInfo(Integer.parseInt(messageArray[4]),
                            Integer.parseInt(messageArray[5]), Double.parseDouble(messageArray[6])
                            , Integer.parseInt(messageArray[7]), Integer.parseInt(messageArray[8]), rovCoord);
                    roverMap.remove(messageArray[1]);
                    roverMap.put(messageArray[1], roverInfo);
                    break;

                //format:SCANCOMPLETE
                case "SCANCOMPLETE":
                    scanComplete=true;
                    break;

                //format:COLLECTED,intNumber,xDouble,yDouble
                case "COLLECTED":
                    Integer resIndex =Integer.parseInt(messageArray[1]);
                    double collectedX = Double.parseDouble(messageArray[2]);
                    double collectedY = Double.parseDouble(messageArray[3]);
                    int remaining =resourceMap.get(resIndex).getAmount() - 1;
                    int type = resourceMap.get(resIndex).getType();
                    resourceMap.remove(resIndex);
                    if (remaining!=0){
                        resourceMap.put(resIndex, new ResourceInfo(type, remaining, new Point2D.Double(collectedX,collectedY))); }
                    break;
            }

        }
        messages.clear();

    }
}
