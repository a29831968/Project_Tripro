package tw.com.flag.tripro.Utils;

import java.util.List;

import tw.com.flag.tripro.models.Route;

/**
 * Created by Tony on 2018/4/24.
 */

public interface DirectionFinderListener {

    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);

}
