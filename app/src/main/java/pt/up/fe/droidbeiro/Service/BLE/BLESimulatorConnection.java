package pt.up.fe.droidbeiro.Service.BLE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import protocolapi.rqst;
import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Communication.ConnectionData;
import pt.up.fe.droidbeiro.Communication.ProtCommConst;

public class BLESimulatorConnection extends Service {

    /**
     * Singleton
     */
    private static BLESimulatorConnection instance = null;

    /**
     * HW Simultator Connection data
     */
    private static final int hw_port=5434;
    private static String hw_addr=null;
    private InetAddress serverAddr;
    private static boolean hw_server_running=false;

    private static final int MESSAGESIZE = 20;

    public Socket hwSimSocket = null;

    //public OutputStream hwOutput = null;
    public PrintWriter hwOutput = null;
    //public InputStream hwInput = null;
    public BufferedReader hwInput = null;

    private static String response = null;


    Client_Socket CS = Client_Socket.getInstance();


    public BLESimulatorConnection() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        System.out.println("I am in on BLESimulatorConnection create");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on BLESimulatorConnection start");

        /**
         * Start HW communication Thread
         */
        ConnectionData CD = ConnectionData.getInstance();
        this.hw_addr="172.30.27.136";//CD.getSERVER_IP();

        Runnable hw_comm = new connect_HW_socket();
        new Thread(hw_comm).start();

        return START_STICKY;
    }

    public class connect_HW_socket implements Runnable{

        @Override
        public void run() {


            while(hwSimSocket==null || !hwSimSocket.isConnected()) {
                try {

                    serverAddr = InetAddress.getByName(hw_addr);
                    Log.e("HW Client", "C: Connecting...::" + hw_addr);

                    hwSimSocket = new Socket(serverAddr, hw_port);
                    hwSimSocket.setKeepAlive(true);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (hwSimSocket.isConnected()){

                Log.e("HW Client", "C: Connected!");
                hw_server_running=true;

                try {
                    hwOutput = new PrintWriter(hwSimSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    hwInput = new BufferedReader(new InputStreamReader(hwSimSocket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while(hw_server_running){

                    Log.e(">>DEGUB:::","Trying to Read Message");

                    try {
                        interpretMessage(readMessage(MESSAGESIZE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    /*
                    rqst new_request = new rqst(ProtCommConst.RQST_ACTION_APP_PACKET_RECEIVED, CS.no_spec, response.getBytes());
                    CS.send_To_Protocol(new_request);*/
                }
            }
        }
    }


    public void sendMessage(byte[] msg) throws IOException {

        Log.e(">>DEBUG::","Sending Message to the Hardware Simulator=="+Arrays.toString(msg));

        hwOutput.println(msg);
        hwOutput.flush();
    }


    private byte[] readMessage(int size) throws IOException{
        //Read message with the desired size from the socket and return it to the caller
        /*The size paramenter allows to read messages consisting of a datagram and not only comands.
        Therefore, this method must be called by the interpretMessage method when he expects
        to receive a datagram and by the run method otherwise*/

        byte[] msg = new byte[size];
        int bytesRead;

        bytesRead = 0;

        /*Try to read as many bytes as the size of an entire message*/
        while(bytesRead!=size){
            bytesRead = bytesRead + this.hwSimSocket.getInputStream().read(msg, bytesRead, size-bytesRead);
        }

        return msg;
    }


    public void interpretMessage(byte[] receivedMessage) throws IOException, ClassNotFoundException {

        int[] msgInt = new int[receivedMessage.length];

        for(int msgnr=0; msgnr<receivedMessage.length; msgnr++)
            msgInt[msgnr] = (int)(receivedMessage[msgnr] & 0xFF);

        System.out.println("Read this message from HW" + Arrays.toString(msgInt));

        Log.e(">>DEGUB::","Received Message from Hardware Simulator");

        //The message is smaller
        int tamanhoReal = 0;

        for (int i=19; i>0; i--)
        {
            if (receivedMessage[i] == 0x0A)
                tamanhoReal = i;
        }

        byte[] realMessage = new byte[tamanhoReal];

        for(int i=0; i<tamanhoReal; i++){
            realMessage[i] = receivedMessage[i];
        }

        rqst pedido = new rqst((byte) 0x00,realMessage[1], realMessage);
        CS.send_To_Protocol(pedido);

    }

    public boolean sendDatagramThroughNetwork(byte[] messageSend) throws IOException {

        byte[] newMessage = new byte[20];

        //Second version
        for(int i=0; i<messageSend.length;i++)
        {
            newMessage[i] = messageSend[i];
        }
        newMessage[messageSend.length] = 0x0A;
        for(int i=messageSend.length+1; i < 20; i++)
        {
            newMessage[i] = 0x00;
        }

        System.out.println("SENT " + Arrays.toString(newMessage));
        sendMessage(newMessage);

        return true;
    }


    public static BLESimulatorConnection getInstance() {
        if (instance==null){
            instance = new BLESimulatorConnection();
        }
        return instance;
    }
}
