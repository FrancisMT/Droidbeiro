package pt.up.fe.droidbeiro.Presentation;

/**
 * Created by Diogo on 04/12/2014.
 */

import android.app.Activity;
import android.app.DialogFragment;
import android.app.IntentService;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Object;
import android.content.ContextWrapper;

import pt.up.fe.droidbeiro.Communication.ProtocoloCommunication;
import pt.up.fe.droidbeiro.R;

import java.util.ArrayList;

import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.BLE.DeviceControlService;
import pt.up.fe.droidbeiro.Service.BLE.RadioControlService;
import pt.up.fe.droidbeiro.Service.BLE.SerialPortService;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 * This activity must be the launcher activity
 *
 *                      Profiles - Heart Rate Profile:
 *
 * Sensor (HR sensor): GATT Server - Peripheral Mode
 * Collector (android): GATT Client - Central Mode
 *
 * More info: https://developer.bluetooth.org/TechnologyOverview/Pages/HRP.aspx
 *
 * -----------------------%%------------------------------------------------------
 *
 *                      Architecture of BLE feature
 *
 *                        ------------------------
 *                       |   DeviceControlService |
 * DeviceScanActivity -> |            +           | -> BluetoothLeService (SampleGattAttributes)
 *                       |   SerialPortService    |
 *                        ------------------------
 *                                    |
 *                                    v
 *                                   APP
 *
 * Important Note: Data from the Heart Rate monitor should be obtained from the DeviceControlService
 *                 with a BroadcastReceiver. The DeviceControlService uses a broadcaster to broadcast the data.
 *                 To read the data from the broadcaster it is only necessary to implement
 *                 the BroadcastReceiver.
 *
 *                 Example of an implementation of the desired Broadcast receiver.
 *                     String hearRate stores the data received by the broadcaster.
 *
 *      private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
 @Override
public void onReceive(Context context, Intent intent) {
final String action = intent.getAction();

 *                 if (DeviceControlService.BROADCAST_ACTION.equals(action)) {
heartRate = intent.getStringExtra(DeviceControlService.HR_DATA));}
 *
 * -----------------------%%------------------------------------------------------
 *
 *                       Classes:
 *
 * DeviceScanActivity (activity): - Searching and displaying available devices
 *
 * DeviceControlService (service) - Connect and get data from the desired characteristic (HR)
 *
 * SerialPortService (service) - Connect and read/write data from the radio module
 *
 * BluetoothLeService (service): - Service for managing connection and data communication with a
 *                                  GATT server hosted on a given Bluetooth LE device.
 *
 * SampleGattAttributes (class): - Subset of standard GATT attributes used in this app.
 *
 *                  Classes used for testing purposes:
 *  (not included in the final APP - Check BLE project if necessary)
 *
 * DeviceControlActivity (activity): - Connect, display data,
 *              and display all GATT services and characteristics supported by the device. [for testing purposes - v1]
 *
 * ControlActivity (activity):  - Connect and display data from the desired characteristic (HR) [for testing purposes - v2]
 *
 */

public class DeviceScanActivity extends ListActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 60 seconds. Can take up to 45 seconds to find device.
    private static final long SCAN_PERIOD = 10000; //10 sec for testing purposes
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private String heartRate="";

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {


        //BLE check
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();




        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Action Bar calls for scanning devices:
        //BLE DEVICE SCAN ------------  REFRESH | SCAN(STOP)

        getMenuInflater().inflate(R.menu.main, menu);

        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_skip).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);


        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_skip).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.action_bar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_skip:
                Intent intent = new Intent(this, Connection.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter - List of available devices.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        /**Intent to DeviceControlService(heartRate) and SerialPortService(radioModule)
         *
         * the condition bellow reads the name of the device in order to determine which service
         * starts first and to avoid launching a service that doesn't match that device.
         *
         * If the name of the device is different from the name of the radio module, it starts the
         * the service responsible for reading the heart rate. Otherwise it starts the service responsible
         * for reading the radio module.
         *
         * Remember to fill the condition in the code bellow with the name of the bluetooth device
         * of the radio module:
         *
         *          else if (!device.getName().equals("name of radio module");
         *
         * Of course, that both services will be launched, when we leave this activity.
         *
         *------------------------------------------//---------------------------------
         *
         * You can also add the intent to other activities here.
        */

        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

        if (device == null) return;

        else if (device.getName().equals("POLAR H7 42E60A1B")) { // name of device == name of HR sensor -> start DeviceControlService
            final Intent intentService = new Intent(this, DeviceControlService.class);

            intentService.putExtra(DeviceControlService.EXTRAS_DEVICE_NAME, device.getName());
            intentService.putExtra(DeviceControlService.EXTRAS_DEVICE_ADDRESS, device.getAddress());

            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }

            startService(intentService);

            Intent intent = new Intent(DeviceScanActivity.this, Connection.class);
            startActivity(intent);

        } else { // name of the device==name of the module radio-> start SerialPortService

            final Intent intentServiceSPP = new Intent(this, SerialPortService.class);
            intentServiceSPP.putExtra(RadioControlService.EXTRAS_DEVICE_NAME_RADIO, device.getName());
            intentServiceSPP.putExtra(RadioControlService.EXTRAS_DEVICE_ADDRESS_RADIO, device.getAddress());

            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            startService(intentServiceSPP);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            //Check if there are devices available

            BluetoothDevice device = mLeDevices.get(i);

            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;

        }
    }
}