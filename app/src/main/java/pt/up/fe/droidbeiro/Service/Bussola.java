package pt.up.fe.droidbeiro.Service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;

/**
 * Created by Edgar on 19/11/2014.
 */
public class Bussola implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor acelarometro;
    public static Float azimut;
    Sensor magnometro;
    float gravity0;
    float gravity1;
    float gravity2;
    float[] mGravity;
    float[] mGeomagnetic;

    public Bussola(){}

    public Bussola(Context context)
    {
        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;

        mSensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometro = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnometro, SensorManager.SENSOR_DELAY_UI);


    }

    public void onSensorChanged(SensorEvent event) {
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
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                azimut = azimut * (float) 57.3248408;

            }

        }
    }


    public String get(){
        return Double.toString(azimut);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
