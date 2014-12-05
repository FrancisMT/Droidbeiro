package pt.up.fe.droidbeiro.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;



/**
 * Created by Edgar on 18/11/2014.
 */
public class GPS extends Service {
    public static LocationManager lManager;
    public static double longitude;
    public static double latitude;
    private final IBinder myBinder = new LocalBinder();

    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public GPS getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPS.this;
        }
    }

    public void IsBoundable() {
        Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
    }


    public void onStartCommand(){
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Toast.makeText(getApplicationContext(), "Por favor introduza o par IP/Porta", Toast.LENGTH_LONG).show();
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
