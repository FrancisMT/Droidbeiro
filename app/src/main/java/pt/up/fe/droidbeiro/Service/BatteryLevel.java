package pt.up.fe.droidbeiro.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.LowBatteryWarningMessage;

public class BatteryLevel extends Service {

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
        bindService(new Intent(BatteryLevel.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public float getMyBatteryLevel() {
        Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return batteryIntent.getIntExtra("level", -1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("On Service", "Battery Level");

        Runnable connect = new battery();
        new Thread(connect).start();

    }

    public class battery implements Runnable {
        @Override
        public void run() {

            doBindService();

            Calendar cal = Calendar.getInstance();
            int seconds = cal.get(Calendar.SECOND);

            if (seconds==0){
                data_sent=false;
            }else
            if (seconds%10==0/* == 59*/) {
                if (!data_sent) {

                    Log.e("Battery Level: ", String.valueOf(getMyBatteryLevel()));

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
                    data_sent=true;
                }
            }

        }
    }

}
