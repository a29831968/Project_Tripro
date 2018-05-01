package tw.com.flag.tripro.models;

/**
 * Created by Tony on 2018/4/24.
 */

public class Itinerary {

    private String origin;
    private String destination;

    private double origin_lat;
    private double origin_long;
    private double destination_lat;
    private double destination_long;
    private String duration;
    private String distance;

    public Itinerary(){


    }

    public Itinerary(String origin, String destination, double origin_lat, double origin_long, double destination_lat
    , double destination_long, String duration, String distance){

        this.origin=origin;
        this.destination=destination;
        this.origin_lat=origin_lat;
        this.origin_long=origin_long;
        this.destination_lat=destination_lat;
        this.destination_long=destination_long;
        this.duration=duration;
        this.distance=distance;
    }

    public double getOrigin_lat() {
        return origin_lat;
    }

    public void setOrigin_lat(double origin_lat) {
        this.origin_lat = origin_lat;
    }

    public double getOrigin_long() {
        return origin_long;
    }

    public void setOrigin_long(double origin_long) {
        this.origin_long = origin_long;
    }

    public double getDestination_lat() {
        return destination_lat;
    }

    public void setDestination_lat(double destination_lat) {
        this.destination_lat = destination_lat;
    }

    public double getDestination_long() {
        return destination_long;
    }

    public void setDestination_long(double destination_long) {
        this.destination_long = destination_long;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }



    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
