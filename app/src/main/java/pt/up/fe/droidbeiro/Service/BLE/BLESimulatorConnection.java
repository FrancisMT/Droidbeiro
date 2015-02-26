package pt.up.fe.droidbeiro.Service.BLE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BLESimulatorConnection extends Service {
    public BLESimulatorConnection() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
