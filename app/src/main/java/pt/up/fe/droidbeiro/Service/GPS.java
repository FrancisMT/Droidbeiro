package pt.up.fe.droidbeiro.Service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Provider;


/**
 * Created by Edgar on 18/11/2014.
 */
public class GPS {
    public static LocationManager lManager;
    public static double longitude;
    public static double latitude;

    public GPS (){

    }

        public GPS (Context context) {
        lManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        LocationListener lListener = new LocationListener() {

            public void onLocationChanged(Location locat) {
                longitude = (double) (locat.getLongitude());
                latitude = (double) (locat.getLatitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

    }




    public Double getLatitude() {
        Location location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            latitude = location.getLatitude();
            return latitude;
        }
        else
            return 0d;
    }

    public Double getLongitude()
    {
        Location location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if(location != null) {
        longitude = location.getLongitude();
        return longitude;
    }
    else
            return 0d;
}

}
