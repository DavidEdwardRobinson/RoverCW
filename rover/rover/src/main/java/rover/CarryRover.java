package rover;

import java.util.Random;
/*
Start after scan complete message received
When collecting a resource

 */

public class CarryRover extends MyRover {

    public CarryRover() {
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

    void collectDecision(){
        //calc if enough energy to get to base with resource and another resource
        //if this rover is closest to resource, go collect it
        //if this rover has the capacity
        if (roverInfo.getCapacity() ==0){return;}

    }


    @Override
    void begin() {
        //called when the world is started
        getLog().info("BEGIN!");

        try {
            //move somewhere initially
            move(5,5,2);
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
        // This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());

        if(pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
            return;
        }

        switch(pr.getResultType()) {
            case PollResult.MOVE:
                //move finished
                getLog().info("Move complete.");

                //now scan
                try {
                    getLog().info("Scanning...");
                    scan(4);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case PollResult.SCAN:
                getLog().info("Scan complete");

                for(ScanItem item : pr.getScanItems()) {
                    if(item.getItemType() == ScanItem.RESOURCE) {
                        getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset() + " Type: "+item.getResourceType());
                        if (item.getxOffset() < 0.1 && item.getyOffset() < 0.1) {
                            try {
                                getLog().info("Attempting a collect!");
                                collect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        } else {
                            try {
                                getLog().info("Moving to resource.");
                                move(item.getxOffset(), item.getyOffset(), 4);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    } else if(item.getItemType() == ScanItem.BASE) {
                        getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
                    } else {
                        getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());
                    }
                }

                // now move again
                Random rand = new Random();
                try {
                    getLog().info("Moving...");
                    move(5 * rand.nextDouble(), 5 * rand.nextDouble(), 4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PollResult.COLLECT:
                getLog().info("Collect complete.");
                try {
                    getLog().info("Moving...");
                    move(1,1,4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PollResult.DEPOSIT:
                getLog().info("Deposit complete.");
                break;
        }

    }

}
