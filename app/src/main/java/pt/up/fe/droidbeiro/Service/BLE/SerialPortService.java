package pt.up.fe.droidbeiro.Service.BLE;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import protocolapi.rqst;
import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Communication.ProtCommConst;
import pt.up.fe.droidbeiro.Messages.ExitAlertMessage;
import pt.up.fe.droidbeiro.Messages.HeartRateAlertMessage;
import pt.up.fe.droidbeiro.Messages.HeartRateMessage;


/**
 *  v2: Notice: This class no longer implements the SPP service, instend it contains a custom GATT profile.
 *  In order to keep the changes to a minimum,the name of the class remains the same.
 *
 *  There are no guarantees that the custom GATT profile implemented bellow is working as intended, as it was impossible
 *  to run the desired tests
 *
 * ------------------------------------------------------------------------------------------------
 *  v1:
 *
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

    // SPP SERVICE - CÓDIGO ANTERIOR - APENAS PARA REFERÊNCIA
    public final static String ACTION_GATT_CONNECTED_RADIO =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED_RADIO";
    public final static String ACTION_GATT_DISCONNECTED_RADIO =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED_RADIO";
    public final static String ACTION_GATT_SERVICES_DISCOVERED_RADIO =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_RADIO";
    public final static String ACTION_DATA_AVAILABLE_RADIO =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_RADIO";
    // ------ RADIO MODULE VARIABLES
    public final static String EXTRA_DATA_RADIO =
            "com.example.bluetooth.le.EXTRA_DATA"; // Caracteriticas adicionais
    public final static String TX_DATA = "com.example.bluetooth.le.TX_DATA"; // caracteristica para tranferência de dados
    public final static String RX_DATA = "com.example.bluetooth.le.RX_DATA"; // caracteristica para leitura de dados
    public final static String RADIO_BATTERY_DATA = "com.example.bluetooth.le.RX_DATA"; //caracteristica para leitura do nivel da bateira
    public final static UUID UUID_SERVICE = UUID.fromString(SampleGattAttributes.RADIO_MODULE_SERVICE);
    public final static UUID UUID_TX_DATA = UUID.fromString(SampleGattAttributes.TX_DATA_CHAR_UUID);
    public final static UUID UUID_RX_DATA = UUID.fromString(SampleGattAttributes.RX_DATA_CHAR_UUID);
    public final static UUID UUID_BATTERY_LEVEL_RADIO = UUID.fromString(SampleGattAttributes.BATT_LEVEL_CHAR_UUID);
    public static final String BROADCAST_ACTION_WRITE = "com.example.bluetooth.le.SERIAL_PORT_WRITE";
    public final static String ACTION_DATA_WRITTEN_RADIO =
            "com.example.bluetooth.le.ACTION_DATA_WRITTEN_RADIO";
    //protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     *
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
     String dataToWrite = intent.getStringExtra("DATA_TO_BT");
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
     } **/

    //----------------------------------------------------------------------

    //Custom GATT profile - CÓDIGO ATUAL
    // Coisas a saber:
    // O BLE 4.0 é constituido por perfis GATT (neste caso é o peril por nós criado: GATT custom)
    // Cada perfil tem um cinjunto de SERVICES, no caso do sensor tinhamos um serviço para a leitura do HR,
    // outro para leitura da bateria, etc. Neste caso temos apenas um serviço para tudo (RADIO_MODULE_SERVICE),
    // as funcionalidades disponiveis por esses serviços chama-se CHARACTERISTIC e é isto que nos interessa.
    // Cada serviço e caracteritica tem um código UUID a si associado, este código tem de ser o mesmo no android e no
    // modulo de radio e é ele que permite identificar que serviços e caracteristicas estamos a usar no momento. Os UUIDs
    // encontram-se definidos na classe SampleGattAttributes.
    // As caracteriticas do RADIO_MODULE_SERVICE que nos interessam são a TX_DATA (para envio), a RX_DATA(para recepção)
    // e a BATT_LEVEL (para leitura de bateria)[não necessário implementar]. O grupo de HW definiu mais que podem ser consultadas
    // no documento que te vou enviar, mas não devemos precisar delas. (Ver SampleGattAttributes com os UUIDS das caracteristicas)
    // Finalmente cada caracteristica é constituida por VALUES(informação sobre caracteristica - readable/writable/suporte para notificações -
    // ou seja: dados recebidos ou a enviar e notificações) e por DESCRIPTORS ( que permitem a configuração de valores especificos e a indicação
    // do tipo de valores).
    //
    // Tens isto melhor explicado neste link: http://possiblemobile.com/2013/12/bluetooth-smart-for-android/
    //
    // Falta apenas dizer que o android está a funcionar em modulo CENTRAL, o que siginifica que é um cliente que se conecta a um PERIPHERAL
    // (modulo de rádio) para leitura e escrita de dados, numa lógica de cliente-servidor.
    //
    // Para esta implementação, os dados são enviados em pacotes de 20bytes. Não te intressa o que vai lá dentro, pois essa intepretação é feita noutros sitios,
    // Sempre que exista algo para enviar, colocam-se os dados num pacotes de 20 bytes e envia-se, na recepção, vamos receber pacotes de 20 bytes e passá-los à aplicação.
    // Qualquer alteração no tamanho dos pacotes tem de ser discutida com o grupo de gestão e HW.
    // No entanto, convém verificares com o francisco se a aplicação está preparada para receber estes pacotes de 20bytes, caso não esteja, fazes o parse necessário.
    //
    // Em principio não precisas de te preocupar com mais nenhuma parte do código, pois todas as alterações necessárias serão feitas nesta classe.
    // O teu trabalho é basicamente acabar de implementar a leitura e escrita de caracteristicas.
    //
    // Ver link: http://stackoverflow.com/questions/24008249/android-ble-read-and-write-characteristics
    //
    // Qualquer problema relacioando com estrutra do perfil GATT custom, terá de ser discutido com o grupo de gestão e de HW. Tem atenção que os UUIDS, podem ser
    // alterados, entre outras coisas. A lógica no entanto, deverá ser sempre a mesma.
    //
    // Finalmente, vai ser praticamente impossível testar isto, mas se o conseguires fazer, óptimo.
    //
    // Vê com atenção o código até ao fim e todos os comentários que fiz, a inicio é chato, mas depois de perceberes torna-se simples.

    // No final lê as coisas a fazer, em baixo, e bom trabalho. :)



    // Coisas a fazer:
    // 1 - Verificar o que falta implementar na escrita de dados (a enviar para o modulo de radio) e implementar
    // 2 - Adaptar o tipo de dados recebidos/enviados para 20 bytes. Fazer conversão e parses necessários
    // 3 - Verificar com o francisco como são passadosos os dados recebidos do modulo, para a aplicação e como são
    //    recebidos os dados da aplicação, para envio para o modulo.
    // 4 - Verificar e corrigir quaisquer erros detectados.


    private final static String TAG = SerialPortService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED_RADIO;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.e("RADIO_BT", "Connected to GATT server.");

                doBindService();
/*
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */

                // Attempts to discover services after successful connection.
                Log.e("RADIO_BT", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED_RADIO;
                mConnectionState = STATE_DISCONNECTED;
                Log.e("RADIO_BT", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }
/*
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {



            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "nooooo if: " + status);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED_RADIO);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);

            }
        }
*/

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            BluetoothGattService service = gatt.getService(UUID_SERVICE);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_RX_DATA);
                if (characteristic != null) {
                    if (gatt.setCharacteristicNotification(characteristic, true) == true) {

                        Log.d("gatt", "SUCCESS!");
                    } else {
                        Log.d("gatt", "FAILURE!");
                    }
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
                    if (0 != (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
                        // It's an indicate characteristic
                        Log.d("onServicesDiscovered", "Characteristic (" + characteristic.getUuid() + ") is INDICATE");
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    } else {
                        // It's a notify characteristic
                        Log.d("onServicesDiscovered", "Characteristic (" + characteristic.getUuid() + ") is NOTIFY");
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }


                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.w(TAG, "nooooo if: " + status);
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED_RADIO);
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);

                    }

                }
            }
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            Log.e("onDescriptorRead","Read method gets called.");
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                //int mensagem;
                //mensagem = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                Log.e(":::::DEBUG::","ON__onCharacteristicRead" );
                broadcastUpdate(ACTION_DATA_AVAILABLE_RADIO, characteristic);
            }
            else
            {
                Log.e("::::EDGAR", "OI");
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {


            }
        }



        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE_RADIO, characteristic);
            Log.e("DADADADA", "DUDUDUDUDU");
        }
    };

    public static byte [] dataToWrite = null;

    private final BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("DEBUG::","I'm Serial Port BroadcastReceiver");

            final String action = intent.getAction();

            if (SerialPortService.BROADCAST_ACTION_WRITE.equals(action)) {
                //dataToWrite = intent.getStringExtra("DATA_TO_BT");
                BluetoothGattService service = mBluetoothGatt.getService(UUID_SERVICE);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_TX_DATA);

                writeCharacteristic(characteristic);
                Log.e("DEBUG::","RECEIVED DATA TO SEND TO RADIO");
            }
        }
    };
    private final IBinder mBinder = new LocalBinder();
    public int DEFAULT_BYTES_IN_CONTINUE_PACKET=20;
    public Client_Socket CS = null;

    //------------------------
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
    boolean CSisBound;


    //-----------------VERIFICAR COM O FRANCISCO COMO SÂO RECEBIDOS OS DADOS DA APP
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    //---------------------------------------------------------------

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**Broadcast receiver, receives data to be sent to the radio module from the app
     *
     * @dataToWrite - String stores the data received from the app, in order to send it to the radio module
     *                via and writeCharacteristic()
     *                It may be neceessary to implement onCharacteristiWrite() to check if the data is written
     */

    @Override
    public void onCreate() {
        super.onCreate();

        //registerReceiver(UpdateReceiver, null);

        //registerReceiver(UpdateReceiver, makeGattUpdateIntentFilter());

        //registerReceiver(UpdateReceiver, new IntentFilter("DATA_TO_BT"));
        Log.e("DEBUG::","In BT SerialPortService");


        /*if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/


    }

    private void doBindService() {
        bindService(new Intent(SerialPortService.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, // LEITURA DE DADOS
                                 final BluetoothGattCharacteristic characteristic) {
        Log.e("Entrei antes intent::","broadcastUpdate_SERIALPORTSERVICE");
        final Intent intent = new Intent(action);

        // This is special handling for the RX data read  & Battery Level profile.

        Log.e("I'm in::","broadcastUpdate_SERIALPORTSERVICE");

        if (UUID_RX_DATA.equals(characteristic.getUuid())) { //Rx Data = 20 bytes e não string. Necessário corrigir (assim como TX).

            int offset=0;

            byte [] RxData = characteristic.getValue();

            //String RxData = characteristic.getStringValue(offset); // 20 bytes. Fazer parse e conversão necessários
            Log.d(TAG, String.format("Received data: "+RxData));
            Log.wtf("broadcastUpdate", "|||||||||Recebi Hardware||||||||||" + Arrays.toString(RxData));

            byte[] mensagem = RxData;
            int tamanho;
            int i;

            for(tamanho=19; tamanho>=0; tamanho--)
            {
                Log.e("DEBUG::","Tamnho=" + tamanho);
                if(mensagem[tamanho]==0x0A)
                    break;
            }


            byte[] dados_finais = new byte[tamanho];

            for(i=0; i< tamanho; i++ )
            {
                dados_finais[i] = mensagem[i];
            }

            //----- Fim da parte adicionada pelo francisco

            Log.wtf("broadcastUpdate", "||||||Enviei para o protocolo|||||||| "  + Arrays.toString(dados_finais));


            CS.send_To_Protocol(new rqst(ProtCommConst.RQST_ACTION_APP_PACK_MSG, (byte) 0, dados_finais));
            Log.e("DEBUG::", "Received data from RADIO ==> Sent to Protocol");


//            intent.putExtra(RX_DATA, String.valueOf(dados_finais));


        }
        else if(UUID_BATTERY_LEVEL_RADIO.equals(characteristic.getUuid())){ //battery levele of Radio module -
            // não está definida como é feita a leitura do nivel de bateira
            // assumi que é igual ao sensor HR -  podes IGNORAR a leitura da bateria
            // porque não está implementado nenhum alerta na nossa APP
            Log.v(TAG, "characteristic.getStingValue(0) = "+ characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            final int battery = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            intent.putExtra(RADIO_BATTERY_DATA, String.valueOf(battery));

        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA_RADIO, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {


        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() { // Outra classe trata disto, não eliminar mesmo assim.
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }else{
            Log.e("DEBUG::","ON__readCharacteristic");

        }

        if (mBluetoothGatt.readCharacteristic(characteristic)==false){
            Log.e("DEBUG","readCharacteristic is FALSE");
        }

    }

    /** Write characteristic*/ // VERIFICAR! Verificar se é necessário adicionar onCharacteriticisWrite e/ou notificações.

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        dataToWrite=CS.getData_to_BT();

        Log.e("DEBUG::","dataToWrite==" + dataToWrite);

        Log.wtf("writeCharacteristic", "||||||||||||||Write to radio|||||||||||||||" + Arrays.toString(dataToWrite));

        int tamanho = dataToWrite.length;
        Log.e("Tamanho",String.valueOf(tamanho));
        int i =0;
        byte[] mensagem = new byte[20];

        for(i=0; i< tamanho; i++) {
            mensagem[i] = dataToWrite[i];
        }

        mensagem[tamanho] = 0x0A;

        if(tamanho < 20)
        {
            for (i=tamanho+1; i < 20; i++ )
            {
                mensagem[i] =0x00;
            }
        }

        Log.wtf("writeCharacteristic", "||||||||||||||||||||Apos stuffing|||||||||||||||" + Arrays.toString(mensagem));
        characteristic.setValue(mensagem);
        Log.e("OII","jsakdajs");
        mBluetoothGatt.writeCharacteristic(characteristic);
        Log.e("OLA", "SERÀ QUE MANDEI ALgUMA ");


        /*
        if (Long.valueOf( String.valueOf(dataToWrite.length()))  > DEFAULT_BYTES_IN_CONTINUE_PACKET)
        {
            int times = Byte.valueOf(String.valueOf(
                    dataToWrite.length() / DEFAULT_BYTES_IN_CONTINUE_PACKET));
            byte[] sending_continue_hex = new byte[DEFAULT_BYTES_IN_CONTINUE_PACKET];



            for (int time = 0; time <= times; time++) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (time == times) {
                    Log.i(TAG, "LAST PACKET ");
                    int character_length = dataToWrite.length() - DEFAULT_BYTES_IN_CONTINUE_PACKET * times;

                    byte[] sending_last_hex = new byte[character_length];

                    for (int i = 0; i < sending_last_hex.length; i++) {
                        sending_last_hex[i] =
                                dataToWrite.getBytes()[sending_continue_hex.length * time + i];
                    }

                    characteristic.setValue(sending_last_hex);
                } else {
                    for (int i = 0; i < sending_continue_hex.length; i++) {
                        Log.i(TAG, "Send stt : "
                                + (sending_continue_hex.length * time + i));

                        // Get next bytes
                        sending_continue_hex[i] =
                                dataToWrite.getBytes()[sending_continue_hex.length * time + i];
                    }
                    characteristic.setValue(sending_continue_hex);
                }
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
        }
        else {
            characteristic.setValue(dataToWrite.getBytes());
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    */
    }


    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("713D0002-503E-4C75-BA94-3148F18D941E");

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */

    public BluetoothGattService getGattServices(UUID uuid) {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getService(uuid);
    }

    public class LocalBinder extends Binder {
        SerialPortService getService() {
            return SerialPortService.this;
        }
    }


}