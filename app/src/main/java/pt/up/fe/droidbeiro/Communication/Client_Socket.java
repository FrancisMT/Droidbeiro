package pt.up.fe.droidbeiro.Communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidBackendAPI.Packet;
import pt.up.fe.droidbeiro.Messages.AcceptRequestMessage;
import pt.up.fe.droidbeiro.Messages.DenyIDMessage;
import pt.up.fe.droidbeiro.Messages.DenyRequestMessage;
import pt.up.fe.droidbeiro.Presentation.BombeiroMain;
import pt.up.fe.droidbeiro.Presentation.ChefeLF;
import pt.up.fe.droidbeiro.Presentation.Compass;
import pt.up.fe.droidbeiro.R;


/**
 * Protocol Imports
 */
import protocolapi.*;
import G5.BusinessLogic.*;
import G5.Processing.*;
import G5.Protocol.*;
import G5.Receiver.*;
import G5.Routing.*;
import G5.Sender.*;
import G5.SharedSingletons.*;
import G5.Util.*;
import pt.up.fe.droidbeiro.Service.BLE.BLESimulatorConnection;
import pt.up.fe.droidbeiro.Service.BLE.SerialPortService;
import pt.up.fe.droidbeiro.Service.GPS;

public class Client_Socket extends Service{

    /**
     * Protocol Data
     */
    //[APP -> PRO] Responses
    private ObjectInputStream socketAppResp;

    //[APP -> PRO] Requests
    private ObjectOutputStream socketAppReq;

    //[PRO -> APP] Responses
    private ObjectOutputStream socketProResp;

    //[PRO -> APP] Requests
    private ObjectInputStream socketProReq;

    //Socket information
    protected ServerSocket serverSocket;
    private Socket socketApp;
    private Socket socketPro;
    public int portaSocket;

    //Protocol Requests Buffer
    public ConcurrentLinkedQueue<rqst> request_buffer = new ConcurrentLinkedQueue<>();

    //Protocol Choise
    public static boolean PG5;
    public static boolean PG6;

    //No spec for some rqsts
    public final static byte no_spec = (byte)0;

    //Communication Status
    public static boolean GSM_Status=true;
    public static boolean GSM_Status_Changed=false;
    public static boolean socket_accepted=false;

    public static boolean isSocket_accepted() {
        return socket_accepted;
    }

    final ConnectionData newCD = new ConnectionData();

    /**
     * Bluetooth Communication
     */
    public static final String message_to_send=null;

    /**
     * Backend Message Types
     */
    private static int msg_type;
    private final static int prelogin_msg_type = 84;
    private final static int cc_predefined_msg_type = 128;
    private final static int cc_personalised_msg_type = 129;
    private final static int cc_requests_fl_update_msg_type = 130;
    private final static int cc_sends_team_info_msg_type = 131;
    private final static int cc_sends_ff_id_msg_type = 132;
    private final static int cc_denies_login_msg_type = 133;
    private final static int cc_accepts_login_msg_type = 134;
    private final static int cc_requests_movetogps_msg_type = 135;
    private final static int cc_automatic_ack_msg_type = 136;

    /***********************************************************/
    /**
     * Auxiliary Variables
     */
    public static boolean ready_to_read = false;
    public static byte Firefighter_ID=0;
    public static boolean after_login = false;
    public static boolean incorrect_login=false;
    public static boolean correct_login=false;
    public static boolean firefighter_login=false;
    public static boolean teamleader_login=false;

    public static byte Pred_Msg_Type = (byte)0x00;
    public static boolean Pred_Msg_Received = false;
    public static boolean In_Combate_Mode=false;
    public static boolean In_Fire_Line_Update=false;
    public static boolean In_Compass=false;

    public static boolean fireline_update_request=false;
    public static float lat=0;
    public static float lon=0;

    public static boolean compass_request=false;

    /***********************************************************/
    /**
     * Countdown variables
     */
    public static CountDownTimer countDownTimer_LF;
    public static CountDownTimer countDownTimer_Compass;
    public static CountDownTimer countDownTimer_pred_msg;
    public static CountDownTimer countDownTimer_pers_msg;
    public final long startTime = 120 * 1000; //TODO change to (120)
    public final long interval = 30 * 1000;   //TODO change to (30)


    /**
     * Conection to backend
     */
    private Socket cSocket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;
    private int isSocketAlive = 0;
    private static String SERVER_IP;//= "192.168.1.65";
    private static int SERVER_PORT;// = 4200;
    private InetAddress serverAddr;
    public boolean running = false;

    /**
     * Notifications
     */
    private static int MY_NOTIFICATION_ID;
    private NotificationManager notificationManager;
    private Notification myNotification;

    /**
     * Variable for binding to other classes
     */
    private final IBinder myBinder = new LocalBinder();

    /**
     * Just for initial tests
     */
    public String response = "";
    public String new_response="";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public Client_Socket getService() {
            System.out.println("I am in Localbinder ");
            return Client_Socket.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("I am in on create");
        countDownTimer_LF = new MyCountDownTimer(startTime, interval);
        countDownTimer_Compass = new MyCountDownTimer(startTime, interval);
        countDownTimer_pred_msg = new MyCountDownTimer(startTime, interval);
        countDownTimer_pers_msg = new MyCountDownTimer(startTime, interval);
    }

    public void IsBoundable() {
        Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConnectionData currentCD = ConnectionData.getInstance();

        /**
         * Backend Thread
         */
        this.SERVER_IP=currentCD.getSERVER_IP();
        this.SERVER_PORT=currentCD.getSERVER_PORT();
        this.PG5=currentCD.isPROTOCOLG5();
        this.PG6=currentCD.isPROTOCOLG6();

        Log.e("Connection","Data" + SERVER_IP+"::"+SERVER_PORT+"::"+PG5+"::"+PG6);

        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");


        /**
         * Protocol Request Processing Thread
         */
        Runnable prot_req_procss = new processProtocol();
        new Thread(prot_req_procss).start();


        Runnable connect = new connectSocket();
        new Thread(connect).start();

        if (PG5 || PG6) {

            /**
             * Protocol Communication Thread
             */
            try {
                serverSocket = new ServerSocket(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            portaSocket = serverSocket.getLocalPort();
            currentCD.setPortaSocket(portaSocket);

            //Log.e("ProtocoloCommunication", "PortaSocket=" + portaSocket);

            Runnable prot_comm = new ProtocoloCommunication();
            new Thread(prot_comm).start();


            /**
             * GSM check thread
             */
            /*Runnable gsm_stat = new CheckConnectioStatus();
            new Thread(gsm_stat).start();*/

        }

        return START_STICKY;
    }

    public void send_packet(Packet pck_to_send) throws IOException {


        broadcastUpdate(SerialPortService.BROADCAST_ACTION_WRITE, new String(pck_to_send.packetContent));

        System.out.println("Packet Content:" + String.valueOf(pck_to_send.packetContent));

        if (PG5 || PG6) {
            /**
             * The application asks the protocol to pack a message it wants to send
             */

            if(GSM_Status){
                Log.e("SENT TO PROTOCOL::", "GSM is ON");
                setGSM(true);
            }else{
                Log.e("SENT TO PROTOCOL::", "GSM is OFF");
                setGSM(false);
            }

            broadcastUpdate(SerialPortService.BROADCAST_ACTION_WRITE, new String(pck_to_send.packetContent));


            rqst new_request = new rqst(ProtCommConst.RQST_ACTION_APP_PACK_MSG, no_spec, pck_to_send.packetContent);
            send_To_Protocol(new_request);
        }
        else{
            out.writeObject(pck_to_send);
            out.flush();
        }
        Log.e("Sent", "ACK");
    }


    public void send_packet_GSM(Packet pck_to_send) throws IOException {


        System.out.println("Packet Content:" + String.valueOf(pck_to_send.packetContent));

        out.writeObject(pck_to_send);
        out.flush();

        Log.e("Sent", "ACK");
    }

    public String getMessage(){
        return this.response;
    }


    /***********************************************************/
    /**
     * Auxiliary Functions
     */
    public static void setAfter_login(boolean after_login) {
        Client_Socket.after_login = after_login;
    }

    public static boolean isAfter_login() {
        return after_login;
    }

    public static boolean isReady_to_read() {
        return ready_to_read;
    }

    public  byte getFirefighter_ID(){
        return this.Firefighter_ID;
    }

    public static boolean isIncorrect_login() {
        return incorrect_login;
    }

    public static void setIncorrect_login(boolean incorrect_login) {
        Client_Socket.incorrect_login = incorrect_login;
    }

    public static boolean isCorrect_login() {
        return correct_login;
    }

    public static void setCorrect_login(boolean correct_login) {
        Client_Socket.correct_login = correct_login;
    }

    public static boolean isFirefighter_login() {
        return firefighter_login;
    }

    public static void setFirefighter_login(boolean firefighter_login) {
        Client_Socket.firefighter_login = firefighter_login;
    }

    public static boolean isTeamleader_login() {
        return teamleader_login;
    }

    public static void setTeamleader_login(boolean teamleader_login) {
        Client_Socket.teamleader_login = teamleader_login;
    }

    public static byte getPred_Msg_Type() {
        return Pred_Msg_Type;
    }

    public static boolean isPred_Msg_Received() {
        return Pred_Msg_Received;
    }

    public static void setIn_Combate_Mode(boolean in_Combate_Mode) {
        In_Combate_Mode = in_Combate_Mode;
    }

    public static boolean isIn_Combate_Mode() {
        return In_Combate_Mode;
    }

    public static boolean isFireline_update_request() {
        return fireline_update_request;
    }

    public static float getLat() {
        return lat;
    }

    public static float getLon() {
        return lon;
    }

    public static void setIn_Fire_Line_Update(boolean in_Fire_Line_Update) {
        In_Fire_Line_Update = in_Fire_Line_Update;
    }

    public static void setIn_Compass(boolean in_Compass) {
        In_Compass = in_Compass;
    }

    public static boolean isCompass_request() {
        return compass_request;
    }

    public static void cancel_CountDownTimer_Compass() {
        countDownTimer_Compass.cancel();
    }

    public static void cancel_CountDownTimer_pers_msg() {
        countDownTimer_pers_msg.cancel();
    }

    public static void cancel_CountDownTimer_pred_msg() {
        countDownTimer_pred_msg.cancel();
    }

    public static boolean getGSM_Status(){
        return GSM_Status;
    }

    /***********************************************************/

    /**
     * Runnable for Connecting with the Backend
     */
    public class connectSocket implements Runnable {
        @Override
        public void run() {
            try {

                while(true) {

                    while(!running) {
                        if (cSocket == null || !cSocket.isConnected()) {
                            try {

                                serverAddr = InetAddress.getByName(SERVER_IP);
                                Log.e("TCP Client", "C: Connecting...");

                                //create a socket to make the connection with the server
                                cSocket = new Socket(serverAddr, SERVER_PORT);
                                cSocket.setKeepAlive(true);
                                running = true;
                            } catch (Throwable t) {
                            }
                        }
                    }

                    if (cSocket.isConnected()) {
                        Log.e("TCP Client", "C: Connected!");

                        out = new ObjectOutputStream(cSocket.getOutputStream());
                        in = new ObjectInputStream(cSocket.getInputStream());


                        /*************************/
                        while (running) {

                            if (!GSM_Status)
                                running=false;

                            Packet pck_received = (Packet) in.readObject();

                            /**
                             * Recepção do ID
                             */
                            msg_type = (int) (pck_received.packetContent[0]) & (0xFF);

                            if (msg_type == cc_sends_ff_id_msg_type && Firefighter_ID == (byte) 0) {

                                Firefighter_ID = pck_received.packetContent[1];
                                response = "Ligado ao Centro de Controlo";
                                MY_NOTIFICATION_ID = 1;
                                Log.e("DEBUG", "Ligado ao Centro de Controlo");

                                /**
                                 * Start Protocol Service
                                 */
                                ConnectionData CD = ConnectionData.getInstance();
                                CD.setSystem_id(Firefighter_ID);
                                if (PG5) {
                                    Intent Connection = new Intent(Client_Socket.this, ProtocolG5Service.class);
                                    startService(Connection);
                                } else if (PG6) {
                                    Intent Connection = new Intent(Client_Socket.this, ProtocolG6Service.class);
                                    startService(Connection);
                                }

                                /**
                                 * Display Notification
                                 */
                                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                myNotification = new Notification(R.drawable.droidbeiro_app_icon, "Nova Mensagem", System.currentTimeMillis());
                                String notificationTitle = "Nova mensagem";
                                String notificationText = response;
                                Intent myIntent = new Intent(Intent.ACTION_VIEW);

                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                                myNotification.defaults |= Notification.DEFAULT_SOUND;
                                myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                                myNotification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, pendingIntent);
                                notificationManager.notify(MY_NOTIFICATION_ID, myNotification);

                            }
                            //Application already has a Firefighter_ID
                            else if (msg_type == cc_sends_ff_id_msg_type && Firefighter_ID != (byte) 0) {

                                byte new_FF_ID = pck_received.packetContent[1];

                                DenyIDMessage dim = new DenyIDMessage(Firefighter_ID, new_FF_ID);
                                dim.build_denyid_packet();
                                send_packet(dim.getDenyid_packet());
                            } else {

                                if (PG5 || PG6) {
                                    /**
                                     * The application receives a message and delivers it to the protocol.
                                     */
                                    rqst new_request = new rqst(ProtCommConst.RQST_ACTION_APP_PACKET_RECEIVED, no_spec, pck_received.packetContent);
                                    send_To_Protocol(new_request);
                                }else{

                                    ready_to_read = false;
                                    if (pck_received!=null) {
                                        ready_to_read = true;
                                    }

                                    Pred_Msg_Received=false;
                                    fireline_update_request=false;
                                    compass_request=false;
                                    MY_NOTIFICATION_ID=1;

                                    msg_type=(int)(pck_received.packetContent[0])&(0xFF);

                                    switch(msg_type){

                                        case cc_sends_ff_id_msg_type:
                                            Firefighter_ID=pck_received.packetContent[1];

                                            new_response="Ligado ao Centro de Controlo";
                                            Log.e("DEBUG","Response=" + new_response);
                                            MY_NOTIFICATION_ID=1;

                                            break;

                                        case cc_denies_login_msg_type:
                                            incorrect_login=true;
                                            correct_login=false;
                                            Log.e("Incorrect Login", "received");
                                            break;

                                        case cc_accepts_login_msg_type:

                                            incorrect_login=false;
                                            correct_login=true;

                                            Log.e("Correct Login", "received: " + Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]);

                                            if (Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]==(byte)0xFF){
                                                Log.e("Team Leader Login", "received");
                                                teamleader_login=true;
                                            }else
                                            if (Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]==(byte)0x00){
                                                Log.e("Firefighter Login", "received");
                                                firefighter_login=true;
                                            }

                                            break;

                                        case cc_personalised_msg_type:
                                            try {
                                                new_response = new String(Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length), "ISO-8859-1");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            Log.e("DEBUG","Response=" + new_response);
                                            MY_NOTIFICATION_ID=2;
                                            break;

                                        case cc_predefined_msg_type:

                                            MY_NOTIFICATION_ID=3;
                                            Pred_Msg_Type=Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0];
                                            switch (Pred_Msg_Type){

                                                case (byte)0x00:
                                                    new_response="Afirmativo";
                                                    break;
                                                case (byte)0x01:
                                                    new_response="Aguarde";
                                                    break;
                                                case (byte)0x02:
                                                    new_response="Assim Farei";
                                                    break;
                                                case (byte)0x03:
                                                    new_response="Correcto";
                                                    break;
                                                case (byte)0x04:
                                                    new_response="Errado";
                                                    break;
                                                case (byte)0x05:
                                                    new_response="Informe";
                                                    break;
                                                case (byte)0x06:
                                                    new_response="Negativo";
                                                    break;
                                                case (byte)0x07:
                                                    new_response="A Caminho";
                                                    break;
                                                case (byte)0x08:
                                                    new_response="No Local";
                                                    break;
                                                case (byte)0x09:
                                                    new_response="No Hospital";
                                                    break;
                                                case (byte)0x10:
                                                    new_response="Disponível";
                                                    break;
                                                case (byte)0x11:
                                                    new_response="De Regresso";
                                                    break;
                                                case (byte)0x12:
                                                    new_response="INOP";
                                                    break;
                                                case (byte)0x13:
                                                    new_response="No Quartel";
                                                    break;
                                                case (byte)0x14:
                                                    new_response="Necessita de Reforços";
                                                    break;
                                                case (byte)0x15:
                                                    new_response="Casa em Perigo";
                                                    break;
                                                case (byte)0x16:
                                                    new_response="Preciso de Descansar";
                                                    break;
                                                case (byte)0x17:
                                                    new_response="Carro em Perigo";
                                                    break;
                                                case (byte)0x18:
                                                    new_response="Descanse";
                                                    break;
                                                case (byte)0x19:
                                                    new_response="Fogo a Alastrar";
                                                    break;
                                                default:
                                                    new_response="Erro";
                                                    break;
                                            }

                                            if (In_Combate_Mode){
                                                playAudioMessages(Pred_Msg_Type);
                                            }
                                            Log.e("DEBUG","Response=" + new_response);

                                            Pred_Msg_Received=true;
                                            break;

                                        case cc_requests_fl_update_msg_type:
                                            MY_NOTIFICATION_ID=4;
                                            new_response="Actualizar Linha de Fogo";
                                            fireline_update_request=true;
                                            In_Fire_Line_Update=false;
                                            Log.e("DEBUG","Response=" + new_response);

                                            break;

                                        case cc_automatic_ack_msg_type:
                                            MY_NOTIFICATION_ID=5;
                                            new_response="Alerta Recebido pelo CC";
                                            Log.e("DEBUG","Response=" + new_response);
                                            break;

                                        case cc_requests_movetogps_msg_type:
                                            compass_request=true;
                                            In_Compass=false;
                                            MY_NOTIFICATION_ID=6;
                                            new_response="Move to GPS";

                                            byte[] latitude =  Arrays.copyOfRange(pck_received.packetContent, 2, 6);
                                            byte[] longitude =  Arrays.copyOfRange(pck_received.packetContent, 6, 10);

                                            /*ByteBuffer lat_bb = ByteBuffer.wrap(latitude);
                                            lat_bb.order(ByteOrder.LITTLE_ENDIAN);
                                            lat = lat_bb.getFloat();

                                            ByteBuffer lon_bb = ByteBuffer.wrap(longitude);
                                            lon_bb.order(ByteOrder.LITTLE_ENDIAN);
                                            lon = lon_bb.getFloat();
                                            */
                                            lat = ByteBuffer.wrap(latitude).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                            lon = ByteBuffer.wrap(longitude).order(ByteOrder.LITTLE_ENDIAN).getFloat();


                                            newCD.setNew_LAT(lat);
                                            newCD.setNew_LON(lon);

                                            Log.e("DEBUG","Response="+new_response+":::"+"latitude="+lat +":::"+"longitude="+lon);
                                            break;

                                        default:
                                            Log.e("DEBUG::","INVALID MSG TYPE");

                                            break;
                                    }

                                    if(new_response!=null) {
                                        Log.d("response", new_response);
                                        if (!In_Combate_Mode) {
                                            // Send Notification
                                            if (MY_NOTIFICATION_ID==4){
                                                countDownTimer_LF.start();
                                                notification_buttons();
                                            }else
                                            if (MY_NOTIFICATION_ID==6){
                                                countDownTimer_Compass.start();
                                                Log.e("Move_to:", "GPS");
                                                notification_buttons_gps();
                                            }else
                                            if (MY_NOTIFICATION_ID==2){
                                                notification_pers();
                                                countDownTimer_pred_msg.start();
                                            }else
                                            if (MY_NOTIFICATION_ID==3){
                                                notification_pred();
                                                countDownTimer_pred_msg.start();
                                            }
                                            else{
                                                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                myNotification = new Notification(R.drawable.droidbeiro_app_icon, "Nova Mensagem", System.currentTimeMillis());
                                                String notificationTitle = "Nova mensagem";
                                                String notificationText = new_response;
                                                Intent myIntent = new Intent(Intent.ACTION_VIEW);

                                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                                                myNotification.defaults |= Notification.DEFAULT_SOUND;
                                                myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                                                myNotification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, pendingIntent);
                                                notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
                                            }
                                        }
                                    }
                                }
                            }

                            if (!cSocket.isConnected()){
                                running = false;
                                Log.w("DEBUG", "SOCKET IS NOT CONNECTED::::" + isSocketAlive);
                            }
                        }
                    } else {
                        Log.e("CLientSocket", "SOCKET IS NOT CONNECTED::::" + isSocketAlive);
                        running = false;
                        throw new UnknownHostException();
                    }
                }
            } catch (StreamCorruptedException e1) {
                e1.printStackTrace();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runnable for Communicating with the Protocol
     */
    public class ProtocoloCommunication implements Runnable{

        @Override
        public void run() {
            Log.e("I'm in:", "protocol communication run");

            //Initialization of communication streams
            try
            {
                socketPro = serverSocket.accept();
                socketApp = serverSocket.accept();
                Log.e(" Protocol:", "socket accept");
                socket_accepted=true;


                socketPro.setSoTimeout(500); //In milliseconds

                //Create the streams
                socketAppReq = new ObjectOutputStream(socketApp.getOutputStream());
                socketAppResp = new ObjectInputStream(socketApp.getInputStream());
                socketProResp = new ObjectOutputStream(socketPro.getOutputStream());
                socketProReq = new ObjectInputStream(socketPro.getInputStream());

                //Flush streams
                socketAppReq.flush();
                socketAppReq.reset();
                socketProResp.flush();
                socketProResp.reset();

                System.out.println("Connected to Socket " + portaSocket + " successfully");
            }

            catch (IOException e)
            {
                throw new Error("ERROR: Could not open sockets to the Protocol.");
            }

            //Handler
            while (true)
            {
                //Read Requests
                get_From_Protocol();
            }
        }
    }

    /**
     * Send request to the protocol
     * @param request
     */
    public void send_To_Protocol(rqst request)
    {
        try
        {
            if (request!=null){
                Log.e("Request status:" , "not null");
            }

            socketAppReq.writeObject(request);

            // Check Protocol's response
            rspns response = new rspns(socketAppResp.readObject());

            if (response.id != ProtCommConst.RSPN_ACTION_APP_OK){

                System.out.println("\n>> ERROR: Response from the protocol was not OK. Error Code: " + response.id + " " + request.id + " " + request.spec);
            }else{
                System.out.println("\n>> Response from the protocol was OK: " + response.id + " " + request.id + " " + request.spec);
            }

        }

        catch (SocketTimeoutException e)
        {
            throw new Error("ERROR: SocketTimeoutException reading from socketAppResp");
        }

        catch(IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Writing request to the protocol instance.");
        }
    }

    /**
     * Method to retrieve requests from the protocol and write them to the request_buffer
     */
    public void get_From_Protocol()
    {
        rqst request;

        try
        {
            request = new rqst(socketProReq.readObject());

            Log.e("PROTOCOL_DEBUG::","Read request from the protocol.");

            //Reply OK to Protocol
            socketProResp.writeObject(new rspns(ProtCommConst.RSPN_ACTION_APP_OK));

            //Write to buffer
            request_buffer.offer(request);

        }

        catch (SocketTimeoutException e){

        }

        catch (IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Writing the response to the Protocol:" + e);
        }
    }


    /**
     * Warn if GSM is on/off
     * @param gsmOn
     */
    public void setGSM(boolean gsmOn)
    {

        rqst request;

        if (gsmOn == true)
        {
            request = new rqst(ProtCommConst.RQST_ACTION_APP_GSM_CHANGE,
                    ProtCommConst.RQST_SPEC_ANDR_GSM_GAINED);
        }
        else
        {
            request = new rqst(ProtCommConst.RQST_ACTION_APP_GSM_CHANGE,
                    ProtCommConst.RQST_SPEC_ANDR_GSM_LOST);
        }


        // Send request and check response
        try
        {
            socketAppReq.writeObject(request);

            // Check if response is OK
            rspns response = new rspns(socketAppResp.readObject());

            if (response.id != ProtCommConst.RSPN_ACTION_APP_OK)
            {
                System.out.println("\n>> ERROR: (GSM_CHANGED) Response from the protocol was not OK.");
            }else{
                System.out.println("\n>> Response from the protocol was OK: " + response.id + " " + request.id + " " + request.spec);
            }

        }
        catch (SocketTimeoutException e)
        {
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new Error("ERROR: Inserting new request (GSM_CHANGE) in the socketAppReq.");
        }
    }


    public class processProtocol implements Runnable{

        @Override
        public void run() {

            Log.e("I'm in:", "processProtocol run");

            while (true) {
                //Processes and destributes Protocol Requests
                processRequests();
            }
        }
    }

    public void broadcastUpdate(final String action, final String data) {

        Log.e("DEBUG::","Client_Socket BT_broadcastUpdate");

        final Intent intent = new Intent(action);

        intent.putExtra("DATA_TO_BT", data);

        sendBroadcast(intent);
    }

    /**
     * Method to process medium buffer and distribute requests.
     */
    public void processRequests(){

        rqst response = null;

        if (!request_buffer.isEmpty()){

            response = request_buffer.poll();

            //The protocol asks the application to send a message
            if (response.id == ProtCommConst.RQST_ACTION_PRO_SEND_PACKET){

                Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message");

                //Send to Backend through GSM
                if (response.spec == ProtCommConst.RQST_SPEC_ANDR_GSM){

                    Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message through GSM");

                    Packet pck_to_send = new Packet();
                    pck_to_send.hasProtocolHeader=true;
                    pck_to_send.packetContent=response.packet;

                    try {
                        out.writeObject(pck_to_send);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                //Send to Backend through RADIO
                else if (response.spec == ProtCommConst.RQST_SPEC_ANDR_RADIO){
                    //Creat request to Foward to BLUETOOTH


                  /*
                  Log.e("PROTOCOL_DEBUG::","The protocol asks the application to send a message through the RADIO");
                  BLESimulatorConnection BLESC = BLESimulatorConnection.getInstance();

                  //Write to HW Socket
                  try {
                      BLESC.sendDatagramThroughNetwork(response.packet);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  */

                    broadcastUpdate(SerialPortService.BROADCAST_ACTION_WRITE, new String(response.packet));


                }

                else{
                    Log.e("Error:","request.spec is invalid");
                }
            }

            //The protocol unpacks a message and gives it to the application
            else if (response.id == ProtCommConst.RQST_ACTION_PRO_UNPACK_MSG){

                Log.e("PROTOCOL_DEBUG::","The protocol unpacks a message and gives it to the application");

                Packet pck_received = new Packet();
                pck_received.packetContent=response.packet;


                ready_to_read = false;
                if (pck_received!=null) {
                    ready_to_read = true;
                }

                Pred_Msg_Received=false;
                fireline_update_request=false;
                compass_request=false;
                MY_NOTIFICATION_ID=1;

                msg_type=(int)(pck_received.packetContent[0])&(0xFF);

                switch(msg_type){

                    case cc_sends_ff_id_msg_type:
                        Firefighter_ID=pck_received.packetContent[1];

                        new_response="Ligado ao Centro de Controlo";
                        Log.e("DEBUG","Response=" + new_response);
                        MY_NOTIFICATION_ID=1;

                        break;

                    case cc_denies_login_msg_type:
                        incorrect_login=true;
                        correct_login=false;
                        Log.e("Incorrect Login", "received");
                        break;

                    case cc_accepts_login_msg_type:

                        incorrect_login=false;
                        correct_login=true;

                        Log.e("Correct Login", "received: " + Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]);

                        if (Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]==(byte)0xFF){
                            Log.e("Team Leader Login", "received");
                            teamleader_login=true;
                        }else
                        if (Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0]==(byte)0x00){
                            Log.e("Firefighter Login", "received");
                            firefighter_login=true;
                        }

                        break;

                    case cc_personalised_msg_type:
                        try {
                            new_response = new String(Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length), "ISO-8859-1");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        Log.e("DEBUG","Response=" + new_response);
                        MY_NOTIFICATION_ID=2;
                        break;

                    case cc_predefined_msg_type:

                        MY_NOTIFICATION_ID=3;
                        Pred_Msg_Type=Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length)[0];
                        switch (Pred_Msg_Type){

                            case (byte)0x00:
                                new_response="Afirmativo";
                                break;
                            case (byte)0x01:
                                new_response="Aguarde";
                                break;
                            case (byte)0x02:
                                new_response="Assim Farei";
                                break;
                            case (byte)0x03:
                                new_response="Correcto";
                                break;
                            case (byte)0x04:
                                new_response="Errado";
                                break;
                            case (byte)0x05:
                                new_response="Informe";
                                break;
                            case (byte)0x06:
                                new_response="Negativo";
                                break;
                            case (byte)0x07:
                                new_response="A Caminho";
                                break;
                            case (byte)0x08:
                                new_response="No Local";
                                break;
                            case (byte)0x09:
                                new_response="No Hospital";
                                break;
                            case (byte)0x10:
                                new_response="Disponível";
                                break;
                            case (byte)0x11:
                                new_response="De Regresso";
                                break;
                            case (byte)0x12:
                                new_response="INOP";
                                break;
                            case (byte)0x13:
                                new_response="No Quartel";
                                break;
                            case (byte)0x14:
                                new_response="Necessita de Reforços";
                                break;
                            case (byte)0x15:
                                new_response="Casa em Perigo";
                                break;
                            case (byte)0x16:
                                new_response="Preciso de Descansar";
                                break;
                            case (byte)0x17:
                                new_response="Carro em Perigo";
                                break;
                            case (byte)0x18:
                                new_response="Descanse";
                                break;
                            case (byte)0x19:
                                new_response="Fogo a Alastrar";
                                break;
                            default:
                                new_response="Erro";
                                break;
                        }

                        if (In_Combate_Mode){
                            playAudioMessages(Pred_Msg_Type);
                        }
                        Log.e("DEBUG","Response=" + new_response);

                        Pred_Msg_Received=true;
                        break;

                    case cc_requests_fl_update_msg_type:
                        MY_NOTIFICATION_ID=4;
                        new_response="Actualizar Linha de Fogo";
                        fireline_update_request=true;
                        In_Fire_Line_Update=false;
                        Log.e("DEBUG","Response=" + new_response);

                        break;

                    case cc_automatic_ack_msg_type:
                        MY_NOTIFICATION_ID=5;
                        new_response="Alerta Recebido pelo CC";
                        Log.e("DEBUG","Response=" + new_response);
                        break;

                    case cc_requests_movetogps_msg_type:
                        compass_request=true;
                        In_Compass=false;
                        MY_NOTIFICATION_ID=6;
                        new_response="Move to GPS";

                        byte[] latitude =  Arrays.copyOfRange(Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length), 0, 4);
                        byte[] longitude =  Arrays.copyOfRange(Arrays.copyOfRange(pck_received.packetContent,2,pck_received.packetContent.length), 4, 9);

                       /* ByteBuffer lat_bb = ByteBuffer.wrap(latitude);
                        lat_bb.order(ByteOrder.LITTLE_ENDIAN);
                        lat = lat_bb.getFloat();

                        ByteBuffer lon_bb = ByteBuffer.wrap(longitude);
                        lon_bb.order(ByteOrder.LITTLE_ENDIAN);
                        lon = lon_bb.getFloat();*/

                        lat = ByteBuffer.wrap(latitude).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                        lon = ByteBuffer.wrap(longitude).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        newCD.setNew_LAT(lat);
                        newCD.setNew_LON(lon);


                        Log.e("DEBUG","Response="+new_response+":::"+"latitude="+lat +":::"+"longitude="+lon);
                        break;

                    default:
                        Log.e("DEBUG::","INVALID MSG TYPE");

                        break;
                }

                if(new_response!=null) {
                    Log.d("response", new_response);
                    if (!In_Combate_Mode) {
                        // Send Notification
                        if (MY_NOTIFICATION_ID==4){
                            countDownTimer_LF.start();
                            notification_buttons();
                        }else
                        if (MY_NOTIFICATION_ID==6){
                            countDownTimer_Compass.start();
                            Log.e("Move_to:", "GPS");
                            notification_buttons_gps();
                        }else
                        if (MY_NOTIFICATION_ID==2){
                            notification_pers();
                            countDownTimer_pred_msg.start();
                        }else
                        if (MY_NOTIFICATION_ID==3){
                            notification_pred();
                            countDownTimer_pred_msg.start();
                        }
                        else{
                            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            myNotification = new Notification(R.drawable.droidbeiro_app_icon, "Nova Mensagem", System.currentTimeMillis());
                            String notificationTitle = "Nova mensagem";
                            String notificationText = new_response;
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);

                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                            myNotification.defaults |= Notification.DEFAULT_SOUND;
                            myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                            myNotification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, pendingIntent);
                            notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
                        }
                    }
                }
            }

            //Unknown Request
            else {
                Log.e("Error:","Unknown requestID read from the socketProReq");
            }




        }

    }

    public void disconnect() throws IOException {

        this.running=false;

        try {
            cSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cSocket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            cSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cSocket = null;
    }

    public void notification_buttons(){

        String resp;
        Intent intent;

        resp="Actualizar linha de fogo";
        intent = new Intent(this, ChefeLF.class);

        // intent triggered, you can add other intent for other actions
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //
        Intent intent_delete = new Intent(this, MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent_delete, 0);


        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(resp)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                        //.addAction(0, "Aceitar", pIntent)
                .setDeleteIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
    }

    public void notification_buttons_gps(){

        String resp;
        Intent intent_gps;

        resp="Mover para coordenada";
        intent_gps = new Intent(this, Compass.class);

        // intent triggered, you can add other intent for other actions
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent_gps, 0);
/*
        //
        Intent intent_delete = new Intent(this, MyBroadcastReceiver_gps.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent_delete, 0);

        //
        Intent intent_ack = new Intent(this, MyBroadcastReceiver_gps_ack.class);
        PendingIntent pendingIntent_ack = PendingIntent.getBroadcast(this, 0, intent_ack, 0);
*/

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(resp)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pIntent)
                        // .setContentIntent(pendingIntent_ack)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                        // .setDeleteIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
    }

    public void notification_pers(){
/*
        Intent intent;

        intent = new Intent(this, ChefeLF.class);

        // intent triggered, you can add other intent for other actions
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //
        Intent intent_delete = new Intent(this, MyBroadcastReceiver_normal.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent_delete, 0);


        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(new_response)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                        // .setContentIntent(pIntent)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                        //.addAction(0, "Aceitar", pIntent)
                .setDeleteIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
        */

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotification = new Notification(R.drawable.droidbeiro_app_icon, "Nova Mensagem", System.currentTimeMillis());
        String notificationTitle = "Nova mensagem";
        String notificationText = new_response;
        Intent myIntent = new Intent(Intent.ACTION_VIEW);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        myNotification.defaults |= Notification.DEFAULT_SOUND;
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        myNotification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, pendingIntent);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }

    public void notification_pred(){

        /*
        Intent intent;

        intent = new Intent(this, ChefeLF.class);

        // intent triggered, you can add other intent for other actions
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //
        Intent intent_delete = new Intent(this, MyBroadcastReceiver_normal_2.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent_delete, 0);


        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(new_response)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                        // .setContentIntent(pIntent)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                        //.addAction(0, "Aceitar", pIntent)
                .setDeleteIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
        */

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotification = new Notification(R.drawable.droidbeiro_app_icon, "Nova Mensagem", System.currentTimeMillis());
        String notificationTitle = "Nova mensagem";
        String notificationText = new_response;
        Intent myIntent = new Intent(Intent.ACTION_VIEW);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        myNotification.defaults |= Notification.DEFAULT_SOUND;
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        myNotification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, pendingIntent);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
    }


    public class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");

            if(!In_Fire_Line_Update) {
                DenyRequestMessage dr_msg = new DenyRequestMessage(Firefighter_ID);
                try {
                    dr_msg.build_denyrequest_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    send_packet(dr_msg.getDenyrequest_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MyBroadcastReceiver_gps extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");

            if (!In_Compass) {
                DenyRequestMessage dr_msg = new DenyRequestMessage(Firefighter_ID);
                try {
                    dr_msg.build_denyrequest_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    send_packet(dr_msg.getDenyrequest_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MyBroadcastReceiver_gps_ack extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");

            AcceptRequestMessage ar_msg = new AcceptRequestMessage(Firefighter_ID);
            try {
                ar_msg.build_acceptrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                send_packet(ar_msg.getAcceptrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }

            cancel_CountDownTimer_Compass();
            Log.e("Request:", "Accepted");

            Intent intentone = new Intent(context.getApplicationContext(), Compass.class);
            intentone.addFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TASK | */Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentone);
        }
    }

    public class MyBroadcastReceiver_normal extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");

            AcceptRequestMessage ar_msg = new AcceptRequestMessage(Firefighter_ID);
            try {
                ar_msg.build_acceptrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                send_packet(ar_msg.getAcceptrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }

            cancel_CountDownTimer_pers_msg();
            Log.e("Request:", "Accepted");

        }
    }

    public class MyBroadcastReceiver_normal_2 extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Here", "I am here");

            AcceptRequestMessage ar_msg = new AcceptRequestMessage(Firefighter_ID);
            try {
                ar_msg.build_acceptrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                send_packet(ar_msg.getAcceptrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }


            cancel_CountDownTimer_pred_msg();
            Log.e("Request:", "Accepted");
        }
    }

    public void playAudioMessages(int messageID) {

        /**
         *  messageID - ID da mensagem
         *
         * Reproduz mensagem de audio correspondente.
         *
         *
         * **/

         /* Pre-Defined Messages

        0 Need Support
        1 Need to Back Down
        2 Firetruck is in Trouble
        3 Need Aerial Support
        4 Go Rest
        5 Aerial Support Incoming
        6 The Fire is Spreading
        7 We’re Leaving
        8 Fire Getting Close to House
        9 House Burned
        10-255 Allocated for Additional Pre-Dened
        Messages */

        //Paths
        String m_0 = "m0_help";
        String m_1 = "m1_back_down";
        String m_2 = "m2_firetruck";
        String m_3 = "m3_aerial";
        String m_4 = "m4_rest";
        String m_5 = "m5_aerial_coming";
        String m_6 = "m6_fire_spreading";
        String m_7 = "m7_leaving";
        String m_8 = "m8_close_to_house";
        String m_9 = "m9_house_burned";

        ArrayList<String> audioMessagesList = new ArrayList<String>(10);

        audioMessagesList.add(0,m_0);
        audioMessagesList.add(1,m_1);
        audioMessagesList.add(2,m_2);
        audioMessagesList.add(3,m_3);
        audioMessagesList.add(4,m_4);
        audioMessagesList.add(5,m_5);
        audioMessagesList.add(6,m_6);
        audioMessagesList.add(7,m_7);
        audioMessagesList.add(8,m_8);
        audioMessagesList.add(9,m_9);

        String path = "android.resource://"+getPackageName()+"/raw/"+audioMessagesList.get(messageID);

        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(getApplicationContext(), Uri.parse(path));
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class MyCountDownTimer extends CountDownTimer {

        public boolean finished=false;

        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        public boolean isFinished() {
            return finished;
        }

        @Override
        public void onFinish() {
            //text.setText("Time's up!");
            Log.e("Countdown Timer", "Times up!");

            finished=true;

            DenyRequestMessage dr_msg = new DenyRequestMessage(Firefighter_ID);
            try {
                dr_msg.build_denyrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                send_packet(dr_msg.getDenyrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            //text.setText("" + millisUntilFinished / 1000);
            if (In_Combate_Mode){
                playAudioMessages(Pred_Msg_Type);
            }
            Log.e("In", "onTick");
        }
    }

    private static Client_Socket instance = null;
    public static Client_Socket getInstance() {
        if(instance == null) {
            instance = new Client_Socket();
        }
        return instance;
    }

}
