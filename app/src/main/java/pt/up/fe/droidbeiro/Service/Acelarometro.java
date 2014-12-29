package pt.up.fe.droidbeiro.Service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.APSAlertMessage;

/**
 * Created by Edgar on 18/11/2014.
 */

public class Acelarometro extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    Sensor acelarometro;
    float gravity0;
    float gravity1;
    float gravity2;
    float[] accelerometer;
    float linear0;
    float linear1;
    float linear2;
    int contador;
    Uri ring;
    Ringtone r;
    CounterClass timer;

    private boolean data_sent = false;

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
        bindService(new Intent(Acelarometro.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("On Service", "APS");
        doBindService();

        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        //timer = new CounterClass(900000000, 100);
        /**
         * For debugging
         */
        timer = new CounterClass(30000, 1000);
        ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), ring);
    }

    /*public Acelarometro(Context context)
    {
        Log.e("On Service", "APS");
        doBindService();

        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;

        mSensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        //timer = new CounterClass(900000000, 100);
        /**
         * For debugging
         */
        /*timer = new CounterClass(30000, 1000);
        ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(context.getApplicationContext(), ring);
    }*/

    public void onSensorChanged(SensorEvent event) {

            accelerometer = event.values;

            float kFilteringFactor = 0.6f;

            gravity0 = (accelerometer[0] * kFilteringFactor) + (gravity0 * (1.0f - kFilteringFactor));
            gravity1 = (accelerometer[1] * kFilteringFactor) + (gravity1 * (1.0f - kFilteringFactor));
            gravity2 = (accelerometer[2] * kFilteringFactor) + (gravity2 * (1.0f - kFilteringFactor));

            linear0 = (accelerometer[0] - gravity0);
            linear1 = (accelerometer[1] - gravity1);
            linear2 = (accelerometer[2] - gravity2);

            float magnitude = 0.0f;
            magnitude = (float) Math.sqrt(linear0 * linear0 + linear1 * linear1 + linear2 * linear2);
            magnitude = Math.abs(magnitude);
            if (magnitude > 0.2) {

                contador = 0;
                r.stop();

            } else {
                if (contador == 0) {
                    timer.start();
                    contador++;
                    data_sent=false;
                } else {
                    contador++;
                }
            }
        }

    public class CounterClass extends CountDownTimer {

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
        }

        @Override
        public void onFinish() {
            r.play();

            if(!data_sent) {
                Log.e("APS", "Alert");

                APSAlertMessage aps_msg = new APSAlertMessage(CS.getFirefighter_ID());
                try {
                    aps_msg.build_apsalert_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(aps_msg.getApsalert_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                data_sent=true;
            }

        }
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void stop() {
// save some information before exit
        // r.stop();
        mSensorManager.unregisterListener(this);
        timer.cancel();
        r.stop();

    }

}
