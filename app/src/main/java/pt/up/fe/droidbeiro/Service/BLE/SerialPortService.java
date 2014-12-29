package pt.up.fe.droidbeiro.Service.BLE;

import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


/**
 *  This class implements the Serial Port Profile (SPP) service.
 *
 *  In this implementation the android is the client (or acceptor)
 *  and the radio module the server (or initiator). The radio module
 *  takes the initiative of starting the connection. The order of the
 *  connection is not dependent of this approach.
 *
 *  It also already implemented a BROADCAST RECEIVER to receive data from the App
 *  and write it to the Server via bluetooth socket and a BROADCASTER to pass on to the App
 *  the data received from the Server via bluetooth socket.
 *
 *  The UUID and MAC address bellow are illustrative and should be
 *  defined according to the server specification.
 *
 *  Please refer to method "onListItemClick(...)"(line 224) of "DeviceScanActivity" in order to
 *  understand how this service is initiated.
 *
 *  Finally, I'm not sure if my approach on how to read the inputStream from the bluetooth socket
 *  is the best (I create a thread that keeps running). Please make sure to take a close look a it (line 204)
 *
 *  For more information:
 *
 *  http://stackoverflow.com/questions/15343369/sending-a-string-via-bluetooth-from-a-pc-as-client-to-a-mobile-as-server
 *  http://digitalhacksblog.blogspot.pt/2012/05/android-example-bluetooth-simple-spp.html
 *  http://developer.android.com/reference/android/bluetooth/BluetoothSocket.html
 *  https://developer.bluetooth.org/TechnologyOverview/Pages/SPP.aspx
 *
*/

public class SerialPortService extends Service {

    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Define according to the server specification
    // Insert your server's MAC address
    private static String address = "00:00:00:00:00:00"; // Define according to the server specification

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private OutputStream outStream = null;
    private InputStream inStream = null;

    private final static String TAG = SerialPortService.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    //Broadcast variables

    public static final String BROADCAST_ACTION_WRITE = "com.example.bluetooth.le.SERIAL_PORT_WRITE";
    public static final String BROADCAST_ACTION_READ = "com.example.bluetooth.le.SERIAL_PORT_WRITE";

    private String mDeviceName;
    private String mDeviceAddress;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up Log messages.
        Log.v(TAG, "Device Name: " + mDeviceName);
        Log.v(TAG, "Device Address: "+mDeviceAddress);

        return 1;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("On Service", "Serial Port Service");

        registerReceiver(UpdateReceiver, null);
        if (mDeviceAddress != null) {

            // Connect to the SPP server
            initialize(); // Get bluetooth Adapter
            boolean result = connect(mDeviceAddress); // Connect and establish communication with the server
            if (result)
                Log.d(TAG, "Connect request result=" + result);
            else
                Log.e(TAG,"Connect request failed");
        }

    }

    //Broadcast receiver
    private final BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
                if (SerialPortService.BROADCAST_ACTION_WRITE.equals(action)) {
                String dataToWrite = intent.getStringExtra("Update this field with the" +
                        "string of data from the app to be sent to the radio module");
                    writeServer(dataToWrite);     //write server
                }
        }
    };


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "SPP: Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "SPP: Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "SPP: BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        startServerConnection(device); // start connection

        if (device == null) {
            Log.w(TAG, "SPP: Device not found.  Unable to connect.");
            return false;
        }
        return true;
    }

    public void startServerConnection(BluetoothDevice device) {

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG,"Socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        mBluetoothAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            mBluetoothSocket.connect();
            Log.v(TAG,"SSP: Connection established and data link opened");
        } catch (IOException e) {
            try {
               mBluetoothSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "SSP: unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        try {
            outStream =  mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "SSP: output stream creation failed:" + e.getMessage() + ".");
        }

        // Create an input Stream
        try {
            inStream =  mBluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "SSP: inpuy stream creation failed:" + e.getMessage() + ".");
        }

        // Initiate readServer Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                readServer();
            }
        }).start();

    }

    private void readServer (){

        String receivedMessage = null;
        byte[] msgReadBuffer = null;

        try {
            inStream.read(msgReadBuffer);
            receivedMessage=msgReadBuffer.toString();
        } catch (IOException e) {
            String msg = "Exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.e(TAG,msg);
        }
        broadcastUpdate(BROADCAST_ACTION_READ, receivedMessage);

    }

    private void writeServer(String message){
        //example
        byte[] msgWriteBuffer = message.getBytes();

        try {
            outStream.write(msgWriteBuffer);
        } catch (IOException e) {
            String msg = "Exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.e(TAG,msg);
        }
    }

    private void broadcastUpdate(final String action, final String data) {
        final Intent intent = new Intent(action);
        intent.putExtra(BROADCAST_ACTION_READ, data);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
