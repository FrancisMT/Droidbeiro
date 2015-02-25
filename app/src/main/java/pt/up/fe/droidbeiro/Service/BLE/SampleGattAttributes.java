package pt.up.fe.droidbeiro.Service.BLE;

/**
 * Created by Diogo on 04/12/2014.
 */

import java.util.HashMap;

/**
 * This class includes the subset of standard GATT attributes used in this app.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";

    public static String TX_DATA_CHAR = "0x1E,0x94,0x8D,0xF1,0x48,0x31,0x94,0xBA,0x75,0x4C,0x3E,0x50,0x01,0x00,0x3D,0x71";
    public static String RX_DATA_CHAR = "0x1E,0x94,0x8D,0xF1,0x48,0x31,0x94,0xBA,0x75,0x4C,0x3E,0x50,0x02,0x00,0x3D,0x71";
    public static String BATT_LEVEL_CHAR = "0x1E,0x94,0x8D,0xF1,0x48,0x31,0x94,0xBA,0x75,0x4C,0x3E,0x50,0x05,0x00,0x3D,0x71";

    // Radio
    //public static String RADIO_MODULE_SERVICE ="0x713d-0000-503e4c75ba943148f18d941e";
    //public static String RADIO_MODULE_SERVICE ="0x1E,0x94,0x8D,0xF1,0x48,0x31,0x94,0xBA,0x75,0x4C,0x3E,0x50,0x00,0x00,0x3D,0x71";
    public static String RADIO_MODULE_SERVICE ="1E0948DF-1483-194B-A754-C3E5000003D71";
    public static String TX_DATA_CHAR_UUID = "1E0948DF-1483-194B-A754-C3E5001003D71";
    public static String RX_DATA_CHAR_UUID = "1E0948DF-1483-194B-A754-C3E5002003D71";
    public static String BATT_LEVEL_CHAR_UUID = "1E0948DF-1483-194B-A754-C3E5005003D71";
    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        //---Radio
        attributes.put("0x713d-0000-503e4c75ba943148f18d941e", "Radio Module Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        //---Radio
        attributes.put(TX_DATA_CHAR_UUID, "TX Data");
        attributes.put(RX_DATA_CHAR_UUID,"RX Data");
        attributes.put(BATT_LEVEL_CHAR_UUID,"Radio Battery Level");


    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}