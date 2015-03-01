package pt.up.fe.droidbeiro.Service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import android.os.Handler;
import android.widget.Toast;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.GPSMessage;
import pt.up.fe.droidbeiro.Messages.LowBatteryWarningMessage;


/**
 * Created by Edgar on 18/11/2014.
 */
public class GPS extends Service{
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
    public double longitude =0;
    public double latitude =0;
    private boolean data_sent = false;
    private boolean battery_level_sent = false;

    Intent intent;

    public Client_Socket CS = null;
    boolean CSisBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            CS = ((Client_Socket.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            CS = null;
        }
    };

    private void doBindService() {
        bindService(new Intent(GPS.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 500);
    }

    public void onCreate() {
        super.onCreate();

        doBindService();
        Log.e("On Service", "GPS");

        intent = new Intent(BROADCAST_ACTION);
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,  new LocationListener() {

            public void onLocationChanged(Location locat) {
                longitude = (double) (locat.getLongitude());
                latitude = (double) (locat.getLatitude());
                Log.e("GPS", "Latitude:"+ latitude);
                Log.e("GPS", "Longitude;" + longitude);
                //broadcastUpdate(BROADCAST_ACTION);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        });
        broadcastUpdate(BROADCAST_ACTION);

    }

    public float getMyBatteryLevel() {
        Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return batteryIntent.getIntExtra("level", -1);
    }

    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        //

        /**
         * For Debuging
         */
        // Location location = lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(latitude == 0 && longitude == 0) {
            Location location = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        }
        String lat = Double.toString(latitude);
        String lon = Double.toString(longitude);
        Log.e("GPS", " A enviar");
        intent.putExtra("LAT", lat);
        intent.putExtra("LONG",lon);
        sendBroadcast(intent);

        /****************************************************************/
        if (CS.isAfter_login() && CS.isSocket_accepted()) {

            if (getMyBatteryLevel() < 15){
                Log.e("Low Battery Level: ", String.valueOf(getMyBatteryLevel()));

                LowBatteryWarningMessage lbw_msg = new LowBatteryWarningMessage(CS.getFirefighter_ID());
                try {
                    lbw_msg.build_LowBatteryWarning_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(lbw_msg.getLowbatterywarning_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            Calendar cal = Calendar.getInstance();
            int seconds = cal.get(Calendar.SECOND);

            if (seconds == 0 || seconds == 30) {
                data_sent = false;
                battery_level_sent = false;

            } else if (seconds == 29 || seconds == 59) {

                if (!data_sent) {
                    Log.e("Latitude sent", lat);
                    Log.e("Longitude sent", lon);

                    GPSMessage gps_msg = new GPSMessage(CS.getFirefighter_ID(), lat, lon);

                    try {
                        gps_msg.build_gps_packet();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        CS.send_packet(gps_msg.getGps_packet());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    data_sent = true;
                }
            }


        }
    }


}