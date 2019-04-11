package barikoi.dscc.dsccholdingtax;

import android.location.Location;

import barikoi.barikoilocation.PlaceModels.Place;

/**
 * Created by Sakib on 3/8/2018.
 */

public interface LocationTaskListener {
    void onStarTask();

    void oneEndTask();

    void reversedAddress(Place address);

    void OnLocationChanged(Location location);
}
