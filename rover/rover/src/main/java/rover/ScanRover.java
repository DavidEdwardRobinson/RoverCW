package rover;

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

    }

    @Override
    void end() {

    }

    @Override
    void poll(PollResult pr) {

    }

    void scanComplete(){
        broadCastToTeam("SCANCOMPLETE");
    }




}
