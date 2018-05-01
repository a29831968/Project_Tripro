package tw.com.flag.tripro.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Tony on 2018/4/25.
 */

public class Route {

    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}