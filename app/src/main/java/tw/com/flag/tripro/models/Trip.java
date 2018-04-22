package tw.com.flag.tripro.models;

/**
 * Created by Tony on 2018/4/20.
 */

public class Trip {
    private String image;
    private String start_date;
    private String trip_day;
    private String trip_name;

    public Trip(String image, String start_date, String trip_day, String trip_name){
        this.image=image;
        this.start_date=start_date;
        this.trip_day=trip_day;
        this.trip_name=trip_name;
    }

    public Trip() {

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getTrip_day() {
        return trip_day;
    }

    public void setTrip_day(String trip_day) {
        this.trip_day = trip_day;
    }

    public String getTrip_name() {
        return trip_name;
    }

    public void setTrip_name(String trip_name) {
        this.trip_name = trip_name;
    }


}
