package rover;

import java.awt.geom.Point2D;

public class ResourceInfo {
    private int type;
    private int amount;
    private Point2D.Double coordinate;

    ResourceInfo(int type, int amount, Point2D.Double coordinate){
        this.type = type;
        this.amount=amount;
        this.coordinate=coordinate;

    }

    public Point2D.Double getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Point2D.Double coordinate) {
        this.coordinate = coordinate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }




}
