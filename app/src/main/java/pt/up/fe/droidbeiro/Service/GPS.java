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
import android.os.Looper;
import android.util.Log;

import java.util.Date;
import android.os.Handler;
import android.widget.Toast;


/**
 * Created by Edgar on 18/11/2014.
 */
public class GPS extends Service {
    public static final String BROADCAST_ACTION = "com.example.GPS.CONTROL_SERVICE_CONNECTED";
    private Runnable sendUpdatesToUI = new Runnable() {

        public void run() {
            broadcastUpdate(BROADCAST_ACTION);
            handler.postDelayed(this, 500);
            // 0.5 seconds
        }
    };
    public static final String LAT = "com.example.GPS.LAT";
    public static final String LONG = "com.example.GPS.LONG";
    public static LocationManager lManager;
    private final Handler handler = new Handler();
    public double longitude;
    public double latitude;
    Intent intent;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 500);
    }

    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener lListener = new LocationListener() {

            public void onLocationChanged(Location locat) {
                longitude = (double) (locat.getLongitude());
                latitude = (double) (locat.getLatitude());
                broadcastUpdate(BROADCAST_ACTION);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };
        broadcastUpdate(BROADCAST_ACTION);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Location location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            longitude = location.getLongitude();
            latitude=location.getLatitude();
        }
        String lat = Double.toString(latitude);
        String lon = Double.toString(longitude);
        intent.putExtra("LAT", lat);
        intent.putExtra("LONG",lon);
        sendBroadcast(intent);
    }


}
