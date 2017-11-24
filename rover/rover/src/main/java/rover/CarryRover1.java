package rover;

import java.util.Random;
/*
Start after scan complete message received
When collecting a resource

 */

public class CarryRover1 extends MyRover {

    public CarryRover1() {
        super();
        scanComplete = false;

        try {
            //set attributes for this rover
            //speed, scan range, max load
            //has to add up to <= 9
            //Fourth attribute is the collector type
            setAttributes(1, 0, 8, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    void begin() {

    }

    @Override
    void end() {

    }

    @Override
    void poll(PollResult pr) {

    }

}
