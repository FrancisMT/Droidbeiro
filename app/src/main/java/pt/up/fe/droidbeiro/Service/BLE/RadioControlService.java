package pt.up.fe.droidbeiro.Service.BLE;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Logic.User;


public class RadioControlService extends Service {
    public static final String BROADCAST_ACTION_RADIO = "com.example.bluetooth.le.RADIO_CONTROL_SERVICE_CONNECTED";
    public static final String RX_DATA = "com.example.bluetooth.le.RX_DATA";
    public static final String EXTRAS_DEVICE_NAME_RADIO = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS_RADIO = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlService.class.getSimpleName();
    private String mDeviceName;
    private String mDeviceAddress;
    private SerialPortService mBluetoothLeService;
    public int charaPropRX;
    public BluetoothGattCharacteristic RxData;
    public BluetoothGattCharacteristic TxData;
    public int charaPropTX_aux;

    Client_Socket CS = null;
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
        bindService(new Intent(RadioControlService.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    private void doUnbindService() {
        if (CSisBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            CSisBound = false;
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((SerialPortService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private boolean mConnected = false;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SerialPortService.ACTION_GATT_CONNECTED_RADIO.equals(action)) {
                mConnected = true;
                Log.v("RadioControlService","Connected");

            } else if (SerialPortService.ACTION_GATT_DISCONNECTED_RADIO.equals(action)) {
                mConnected = false;
                Log.v(TAG,"Disconnected");

            } else if (SerialPortService.ACTION_GATT_SERVICES_DISCOVERED_RADIO.equals(action)) {
                //Call the supported services and characteristics on the user interface.
                getFeatures();
            } else if (SerialPortService.ACTION_DATA_AVAILABLE_RADIO.equals(action)) {
                // displayData(intent.getStringExtra(SerialPortService.EXTRA_DATA));
                // displayDataBat(intent.getStringExtra(BluetoothLeService.EXTRA_DATABAT));
            }
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED_RADIO: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED_RADIO: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED_RADIO: discovered GATT services.
    // ACTION_DATA_AVAILABLE_RADIO: received data from the device.  This can be a result of read
    //                        or notification operations.
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    //More info @ http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SerialPortService.ACTION_GATT_CONNECTED_RADIO);
        intentFilter.addAction(SerialPortService.ACTION_GATT_DISCONNECTED_RADIO);
        intentFilter.addAction(SerialPortService.ACTION_GATT_SERVICES_DISCOVERED_RADIO);
        intentFilter.addAction(SerialPortService.ACTION_DATA_AVAILABLE_RADIO);
        return intentFilter;
    }

    private void getFeatures() {

        //RadioService
        final BluetoothGattService RadioService = mBluetoothLeService.
                getGattServices(UUID.fromString(SampleGattAttributes.RADIO_MODULE_SERVICE));

        RxData = RadioService.
                getCharacteristic(UUID.fromString(SampleGattAttributes.RX_DATA_CHAR_UUID));

        /*final BluetoothGattCharacteristic TxData = RadioService.
                getCharacteristic(UUID.fromString(SampleGattAttributes.TX_DATA_CHAR_UUID));*/

        TxData = RadioService.
                getCharacteristic(UUID.fromString(SampleGattAttributes.TX_DATA_CHAR_UUID));


        final BluetoothGattCharacteristic RadioBatData = RadioService.
                getCharacteristic(UUID.fromString(SampleGattAttributes.BATT_LEVEL_CHAR_UUID));

        charaPropRX = RxData.getProperties();
        final int charaPropTX = TxData.getProperties();
        final int charaPropRx = RxData.getProperties();

        charaPropTX_aux=charaPropTX;

        //final int charaPropBat = RadioBatData.getProperties();

        Log.e("DEBUG", "on getFeatures");
        readGATTCharacteristic(charaPropRx, RxData);

        //writeGATTCharacteristic(charaPropTX, TxData);

        // readGATTCharacteristic(charaPropBat, RadioBatData);

        //imcomplete - everything is done in the SerialPortService Class.

    }

    private void readGATTCharacteristic(int charaProp, BluetoothGattCharacteristic characteristic){

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            Log.e("1","1");
            mBluetoothLeService.readCharacteristic(characteristic);
        }

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
        }
    }

    private void writeGATTCharacteristic(int charaProp, BluetoothGattCharacteristic characteristic){

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            Log.e("2","2");
            Log.e("DEBUG::", "ON writeGATTCharacteristic");
            mBluetoothLeService.writeCharacteristic(characteristic);
        }

        /*if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
        }*/
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("DEBUG::","In RadioControlService onCreate");

        doBindService();

        Intent gattServiceIntent = new Intent(this, SerialPortService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        /**
         * Thread to handle messages to send
         */
        Runnable DS = new data_to_send();
        new Thread(DS).start();

        //Intent broadCastIntent = new Intent(BROADCAST_ACTION);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME_RADIO);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS_RADIO);

        // Sets up Log messages.
        //Log.v(TAG, "Device Name: "+mDeviceName);
        Log.v(TAG, "Device Address: "+mDeviceAddress);

        return 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void displayData(String data) {
        if (data != null) {
            broadcastUpdate(BROADCAST_ACTION_RADIO,data);
            //Log.v(TAG,"Heart Rate Data: "+data);
        }
    }

    /*private void displayDataBat(String data) {
        if (data != null) {
            mDataBattery.setText(data);}
    }*/

    private void broadcastUpdate(final String action, final String data) {
        final Intent intent = new Intent(action);
        intent.putExtra(RX_DATA, data);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**************************************************************************/
    public class data_to_send implements Runnable {
        @Override
        public void run() {

            Log.e("DEBUG::", "In data_to_send");


            while(true){

                if(CS.isMessage_to_BT()){

                    writeGATTCharacteristic(charaPropTX_aux, TxData);

                    CS.setMessage_to_BT(false);
                }

            }
        }
    }
    /**************************************************************************/


}