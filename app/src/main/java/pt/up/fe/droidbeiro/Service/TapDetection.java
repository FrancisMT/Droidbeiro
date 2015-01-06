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
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.AcceptRequestMessage;
import pt.up.fe.droidbeiro.Messages.PersonalizedMessage;
import pt.up.fe.droidbeiro.Messages.SOSMessage;
import pt.up.fe.droidbeiro.Messages.SurroundedByFlamesMessage;

public class TapDetection extends Service implements SensorEventListener {
    /*public TapDetection() {
    }*/

    Sensor accelerometer;
    SensorManager sensorManager;
    //TextView acceleration;
    //TextView signal;
    float[] gravity;
    float[] linear_acceleration;
    long start;
    int state1;
    int state2;
    int state3;
    float intensity;
    float previous;

    Uri ring;
    Ringtone r;

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
        bindService(new Intent(TapDetection.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate(){
        super.onCreate();

        Log.e("On Service", "Tap detection");
        doBindService();
        //bindService(new Intent(TapDetection.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);

        state1 =0;
        state2=0;
        state3=0;
        gravity = new float[3];
        linear_acceleration= new float[3];
        previous = 0f;


        gravity[0]= 0f;
        gravity[1]= 0f;
        gravity[2]= 0f;
        linear_acceleration[0]=0f;
        linear_acceleration[1]=0f;
        linear_acceleration[2]=0f;

        start= System.nanoTime();

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), ring);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = (float) 0.8;
        final long elapsedTime;
        double seconds;


        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2]; //z axis

        linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
        linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
        linear_acceleration[2] = sensorEvent.values[2] - gravity[2]; //z axis


        intensity = (float) (previous - linear_acceleration[2]);
        //acceleration.setText(Float.toString(intensity));

        previous = linear_acceleration[2];

        if (intensity > 3) {

            elapsedTime = System.nanoTime() - start;

            seconds = (double) elapsedTime / 1000000000.0;

            if (seconds < 0.3) {
                //signal.setText("too short");
                return;
            }

            start = System.nanoTime();

            ////////////////////////////
            //Detecao do padrao de OK//
            ///////////////////////////
            if (state1 == 0)
                state1 = 1;
            else if ((state1 == 1) && (seconds < 1) && (seconds > 0.60)) {
                state1 = 2;
                r.stop();
            } else if ((state1 == 2) && (seconds < 0.60) && (seconds > 0.30)) {
                state1 = 3;
                //signal.setText("");
            } else if ((state1 == 3) && (seconds < 0.60) && (seconds > 0.30)) {
                //signal.setText("Message sent: OK");
                r.play();
                state1 = 0;
                ///////////////////////////////////////////////////////////////
                // MSG OK DETETADA COM SUCESSO ESCREVER AQUI FRANCISCO!!!!!! //
                //////////////////////////////////////////////////////////////
                Log.e("TAP Detected:", "OK");
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
                Log.e("ACK", "Sent to CC");

                return;
            }

            else {
                state1 = 1;

            }


            //////////////////////////////////////////
            //Detecao do padrao Surrounded by flames//
            /////////////////////////////////////////
            if (state2 == 0)
                state2 = 1;
            else if ((state2 == 1) && (seconds < 0.60) && (seconds > 0.30)) {
                state2 = 2;
                r.stop();
            } else if ((state2 == 2) && (seconds < 0.60) && (seconds > 0.30)) {
                state2 = 3;
                //signal.setText("");
            } else if ((state2 == 3) && (seconds < 1) && (seconds > 0.60)) {
                //signal.setText("Message sent: SURROUNDED BY FLAMES");
                r.play();
                state2 = 0;

                //Envio da mensagem de Surrounded by flames
                SurroundedByFlamesMessage surrounded_msg = new SurroundedByFlamesMessage(CS.getFirefighter_ID());
                try {
                    surrounded_msg.build_sos_packet();
                } catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(surrounded_msg.getSurroundedbyflames_packet());
                }catch (IOException e){
                    e.printStackTrace();
                }
                Log.e("TAP Detected:", "Surrounded by flames");
                return;
            }
            else {
                state2 = 1;
            }

            ///////////////////////////
            //Detecao do padrao SOS//
            /////////////////////////
            if (state3 == 0)
                state3 = 1;
            else if ((state3 == 1) && (seconds < 0.60) && (seconds > 0.30)) {
                state3 = 2;
                r.stop();
            } else if ((state3 == 2) && (seconds < 0.60) && (seconds > 0.30)) {
                state3 = 3;
                //signal.setText("");
            } else if ((state3 == 3) && (seconds < 0.60) && (seconds > 0.30)) {
                //signal.setText("Message sent: SOS");
                r.play();
                state3 = 0;

                //Envio da mensagem de SOS
                SOSMessage sos_msg = new SOSMessage(CS.getFirefighter_ID());
                try {
                    sos_msg.build_sos_packet();
                } catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(sos_msg.getSos_packet());
                }catch (IOException e){
                    e.printStackTrace();
                }

                Log.e("TAP Detected:", "SOS");
                return;
            }
            else {
                state3 = 1;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
