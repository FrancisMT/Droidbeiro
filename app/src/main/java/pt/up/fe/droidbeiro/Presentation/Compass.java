package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.R;

/**
 * Created by Edgar on 17/12/2014.
 */
public class Compass extends Activity implements SensorEventListener {

    public double longitude=0;
    public double latitude=0;
    protected LocationManager lm;
    float degree;
    double lat1 =41.149686;
    double long1 = -8.59873;
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
    // define the display assembly compass picture
    private ImageView image;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;
        currentDegree = 0f;

        // our compass image
        image = (ImageView) findViewById(R.id.compass);
        text = (TextView) findViewById(R.id.distance);


        // initialize your android device sensor capabilities

        mSensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        Sensor acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnometro = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnometro, SensorManager.SENSOR_DELAY_UI);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude= location.getLongitude();
        latitude = location.getLatitude();

        LocationListener lListener = new LocationListener() {

            public void onLocationChanged(Location locat) {
                longitude = (double) (locat.getLongitude());
                latitude = (double) (locat.getLatitude());
                getDistancia();
                nova_posicao();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };
        if(longitude!=0 && latitude != 0)
        nova_posicao();

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
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

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

    }

    public void getDistancia(){
        double dlon, dlat, a, distancia;
        dlon = lat1 - longitude;
        dlat = long1 - latitude;
        a = Math.pow(Math.sin(dlat/2),2) + Math.cos(latitude) * Math.cos(lat1) * Math.pow(Math.sin(dlon/2),2);
        distancia = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance= 6378140 * distancia; /* 6378140 is the radius of the Earth in meters*/
        text.setText("Distance: " + Double.toString(distance) + " meters");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}


