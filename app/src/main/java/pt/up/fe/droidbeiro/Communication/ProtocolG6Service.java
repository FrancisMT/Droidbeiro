package pt.up.fe.droidbeiro.Communication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import protocolapi.*;

public class ProtocolG6Service extends Service {

    private final boolean machine=false;
    private byte system_id;
    private int portaSocket;


    public ProtocolG6Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.e("On Service", "Protocol G6 Service");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("I'm in:", "protocol G6 service start");

        ConnectionData currentCD = ConnectionData.getInstance();
        this.system_id=currentCD.getSystem_id();
        this.portaSocket=currentCD.getPortaSocket();

        Runnable run_prot = new run_protocol();
        new Thread(run_prot).start();
        return START_STICKY;
    }

    public class run_protocol implements Runnable{

        @Override
        public void run() {

            Log.e("I'm in:", "protocol G6 service run");

            //Initialize Protocol
            Protocol_G6 newProtocol = new Protocol_G6(machine, portaSocket, system_id);
            try {
                newProtocol.execute();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
