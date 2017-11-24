package rover;

public class CarryRover2 extends  MyRover{

    CarryRover2(){
        super();
        scanComplete = false;

        try {
            //set attributes for this rover
            //speed, scan range, max load
            //has to add up to <= 9
            //Fourth attribute is the collector type
            setAttributes(1, 0, 8, 2);
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
