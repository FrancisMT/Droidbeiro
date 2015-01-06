package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.AcceptRequestMessage;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.GPS;

/**
 * Created by Edgar on 17/12/2014.
 */
public class Compass extends Activity implements SensorEventListener {

    private static boolean hotfix=false;
    public double longitude=0;
    public double latitude=0;
    public  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };
    float degree;
    double lat1;
    double long1;
    float gravity0;
    float gravity1;
    float gravity2;
    float[] mGravity;
    float[] mGeomagnetic;
    TextView tvHeading;
    double distancia;
    float angle;
    float antigo;
    double pi = 3.14159265359;
    ImageView image2;
    TextView text;
    float newnorth;
    Client_Socket CS = null;
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
    boolean CSisBound;
    // define the display assembly compass picture
    private ImageView image;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;

    private void doBindService() {
        bindService(new Intent(Compass.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    private void doUnbindService() {
        if (CSisBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            CSisBound = false;
        }
    }

    private void updateUI(Intent intent) {
        latitude = Double.parseDouble(intent.getStringExtra("LAT"));
        longitude = Double.parseDouble(intent.getStringExtra("LONG"));

        Log.e("Mover de coordenadas: ", String.valueOf(latitude) + " || " + String.valueOf(longitude));
        getDistancia();
        //nova_posicao();
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);

        hotfix=true;

        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;
        currentDegree = 0f;

        // our compass image
        image = (ImageView) findViewById(R.id.compass);
        text = (TextView) findViewById(R.id.distance);

        //start service on create
        doBindService();

        CS.setIn_Compass(true);

        lat1=CS.getLat();
        long1=CS.getLon();

        Log.e("Mover de coordenadas: ", String.valueOf(latitude) + " || " + String.valueOf(longitude));
        Log.e("Mover para coordenadas: ", String.valueOf(lat1) + " || " + String.valueOf(long1));

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        Sensor acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnometro = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnometro, SensorManager.SENSOR_DELAY_UI);

        if(longitude!=0 && latitude != 0)
            {
                nova_posicao();
                getDistancia();
            }

    }

    protected void nova_posicao(){
        double lg = (long1- longitude);
        double lt = (lat1-latitude);

        angle = (float)Math.atan(lt/lg);
        angle = (float)(angle / pi)*180;
        if((lg<=0 && lt <0) || (lg <0 && lt >0) || angle ==-0)
            angle = angle +180;
        RotateAnimation ra = new RotateAnimation(antigo,-angle, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        image2.startAnimation(ra);
        antigo = -angle;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GPS.BROADCAST_ACTION));
    }
/*
    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }
*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree=currentDegree;
        // get the angle around the z-axis rotated
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                degree = orientation[0]; // orientation contains: azimut, pitch and roll
                degree = degree * (float) 57.3248408;
                degree = degree -angle+90;
            }
        }



        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // how long the animation will take place
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

        if(hotfix){
            hotfix=false;

            AcceptRequestMessage ar_msg = new AcceptRequestMessage(CS.getFirefighter_ID());
            try {
                ar_msg.build_acceptrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                CS.send_packet(ar_msg.getAcceptrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("Request:", "Accepted");
        }
    }


    public void getDistancia(){
        double dlon, dlat, a, distancia;
        dlon = Math.toRadians(long1) - Math.toRadians(longitude);
        dlat = Math.toRadians(lat1) - Math.toRadians(latitude);
        a = Math.pow(Math.sin(dlat/2),2) + Math.cos(latitude) * Math.cos(lat1) * Math.pow(Math.sin(dlon/2),2);
        distancia = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance= 6378140 * distancia; //6378140 is the radius of the Earth in meters
        text.setText("Distância:\n" + Double.toString((double)Math.round(distance * 100) / 100) + " metros");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

}


