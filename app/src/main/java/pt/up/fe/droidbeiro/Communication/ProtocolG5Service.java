package pt.up.fe.droidbeiro.Communication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import G5.Protocol.Protocol_G5;
import pt.up.fe.droidbeiro.Logic.User;


public class ProtocolG5Service extends Service {

    private final boolean machine=false;
    private byte system_id;
    private int portaSocket;


    public ProtocolG5Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.e("On Service", "Protocol G5 Service");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("I'm in:", "protocol G5 service start");

        Bundle extras = intent.getExtras();
        this.system_id=(byte)extras.get("ID");
        this.portaSocket=(int)extras.get("PORTSOCKET");

        Runnable run_prot = new run_protocol();
        new Thread(run_prot).start();
        return START_STICKY;
    }

    public class run_protocol implements Runnable{

        @Override
        public void run() {

            Log.e("I'm in:", "protocol G5 service run");

            //Initialize Protocol
            Protocol_G5 newProtocol = new Protocol_G5(machine, portaSocket, system_id);
            newProtocol.execute();

        }
    }

}
