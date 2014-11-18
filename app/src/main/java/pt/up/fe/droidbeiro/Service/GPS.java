package pt.up.fe.droidbeiro.Service;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


/**
 * Created by Edgar on 18/11/2014.
 */
public class GPS {
    public LocationManager lManager;
    public double longitude;
    public double latitude;

    public void create(Context context) {
        lManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        LocationListener lListener = new LocationListener() {

            public void onLocationChanged(Location locat) {}

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

    }

    public void setCoordenadas(){
        Location location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        else {
            longitude=0;
            latitude=0;
        }

    }

    public String getLatitude() {
        return Double.toString(latitude);
    }

    public String getLongitude() {
        return Double.toString(longitude);
    }

}
