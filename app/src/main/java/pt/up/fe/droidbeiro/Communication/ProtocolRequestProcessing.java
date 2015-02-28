package pt.up.fe.droidbeiro.Communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ProtocolRequestProcessing extends Service {

    private static Client_Socket instance = null;


    public ProtocolRequestProcessing() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        Log.e("On Service", "ProtocolRequestProcessing Service");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("I'm in:", "protocol ProtocolRequestProcessing service start");

        return START_STICKY;
    }


}
