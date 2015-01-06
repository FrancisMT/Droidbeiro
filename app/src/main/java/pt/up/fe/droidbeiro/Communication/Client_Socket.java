package pt.up.fe.droidbeiro.Communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import pt.up.fe.droidbeiro.Messages.AcceptRequestMessage;
import pt.up.fe.droidbeiro.Messages.DenyIDMessage;
import pt.up.fe.droidbeiro.Messages.DenyRequestMessage;
import pt.up.fe.droidbeiro.Presentation.ChefeLF;
import pt.up.fe.droidbeiro.Presentation.ChefeMain;
import pt.up.fe.droidbeiro.Presentation.Compass;
import pt.up.fe.droidbeiro.Presentation.NotificationRequestResponse;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

public class Client_Socket extends Service{

    private final static byte prelogin_msg_type = (byte)0x84;
    private final static byte cc_predefined_msg_type = (byte)0x128;
    private final static byte cc_personalised_msg_type = (byte)0x129;
    private final static byte cc_requests_fl_update_msg_type = (byte)0x130;
    private final static byte cc_sends_team_info_msg_type = (byte)0x131;
    private final static byte cc_sends_ff_id_msg_type = (byte)0x132;
    private final static byte cc_denies_login_msg_type = (byte)0x133;
    private final static byte cc_accepts_login_msg_type = (byte)0x134;
    private final static byte cc_requests_movetogps_msg_type = (byte)0x135;
    private final static byte cc_automatic_ack_msg_type = (byte)0x136;

    /***********************************************************/
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
    public static double lat=0;
    public static double lon=0;

    public static boolean compass_request=false;
    /***********************************************************/
    public CountDownTimer countDownTimer_LF;
    public static CountDownTimer countDownTimer_Compass;
    public CountDownTimer countDownTimer_pred_msg;
    public CountDownTimer countDownTimer_pers_msg;
    public final long startTime = 30 * 1000; //TODO change to (120)
    public final long interval = 1 * 1000;   //TODO change to (30)


    private Socket cSocket = null;
    //private PrintWriter out = null;
    //private BufferedReader in = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;

    private int isSocketAlive = 0;

    private static String SERVER_IP;//= "192.168.1.65";
    private static int SERVER_PORT;// = 4200;
    private static final int SERVER_TIMEOUT = 1000;

    private boolean dataToSend = false;
    private boolean dataToRead = false;
    private String dataSend = null;
    private String dataRead = null;

    private InetAddress serverAddr;
    private boolean running = false;

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;

    private static int MY_NOTIFICATION_ID;
    private NotificationManager notificationManager;
    private Notification myNotification;

    private final IBinder myBinder = new LocalBinder();

    /**
     * Just for initial tests
     */
    private String msgToServer = null;
    public String response = "";

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

        Bundle extras = intent.getExtras();

        this.SERVER_IP= (String) extras.get("IP");
        this.SERVER_PORT= Integer.parseInt((String) extras.get("PORT"));

        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        return START_STICKY;
    }

    public static void setAfter_login(boolean after_login) {
        Client_Socket.after_login = after_login;
    }

    public static boolean isAfter_login() {
        return after_login;
    }

    /**
     * Justo to test writeObjet
     * @param message_to_send
     * @throws IOException
     */
    public void sendMessage(String message_to_send) throws IOException {
        out.writeObject(message_to_send);
        //out.println(message_to_send);
        out.flush();
    }

    public static void send_packet(Packet pck_to_send) throws IOException {
        out.writeObject(pck_to_send);
        //out.println(message_to_send);
        out.flush();
    }

    public String getMessage(){
        return this.response;
    }


    /***********************************************************/
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

    public static double getLat() {
        return lat;
    }

    public static double getLon() {
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

    public void cancel_CountDownTimer_pers_msg() {
        this.countDownTimer_pers_msg.cancel();
    }

    public void cancel_CountDownTimer_pred_msg() {
        countDownTimer_pred_msg.cancel();
    }

    /***********************************************************/

    public class connectSocket implements Runnable {
        @Override
        public void run() {
            try {
                serverAddr = InetAddress.getByName(SERVER_IP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server
                cSocket = new Socket(serverAddr, SERVER_PORT);
                running = true;

                if (cSocket.isConnected()) {
                    Log.e("TCP Client", "C: Connected!");

                    //out = new PrintWriter(cSocket.getOutputStream(), true);
                    //in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
                    out = new ObjectOutputStream(cSocket.getOutputStream());
                    in = new ObjectInputStream(cSocket.getInputStream());

                    while(running){
                        //response = (String) in.readLine();
                        /*response = (String) in.readObject();
                        Log.d("response", response);*/
                        Packet pck_received = (Packet) in.readObject();
                        ready_to_read = false;
                        if (pck_received!=null) {
                            ready_to_read = true;
                        }

                        Pred_Msg_Received=false;
                        fireline_update_request=false;
                        compass_request=false;
                        response=null;
                        MY_NOTIFICATION_ID=1;

                        switch(pck_received.getMessageType()){

                            case prelogin_msg_type:
                                Firefighter_ID=pck_received.getFirefighterID();

                                response="Ligado ao Centro de Controlo";
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

                                Log.e("Correct Login", "received: " + pck_received.getMessage()[0]);

                                if (pck_received.getMessage()[0]==(byte)0xFF){
                                    Log.e("Team Leader Login", "received");
                                    teamleader_login=true;
                                }else
                                if (pck_received.getMessage()[0]==(byte)0x00){
                                    Log.e("Firefighter Login", "received");
                                    firefighter_login=true;
                                }

                                break;

                            case cc_personalised_msg_type:
                                response = new String(pck_received.getMessage(), "ISO-8859-1");
                                MY_NOTIFICATION_ID=2;
                                break;

                            case cc_predefined_msg_type:

                                /** Message code table
                                 *
                                 * "Preciso de ajuda"           <-> 0
                                 * "Preciso afastar-me"         <-> 1
                                 * "Camião com problemas"       <-> 2
                                 * "Preciso de suporte aéreo"   <-> 3
                                 * "Vai descansar"              <-> 4
                                 * "Suporte Aéreo a chegar"     <-> 5
                                 * "Fogo a espalhar-se"         <-> 6
                                 * "A retirar-me"               <-> 7
                                 * "Fogo perto de casa"         <-> 8
                                 * "Casa queimada"              <-> 9
                                 */

                                MY_NOTIFICATION_ID=3;
                                Pred_Msg_Type=pck_received.getMessage()[0];
                                switch (Pred_Msg_Type){

                                    case (byte)0x00:
                                        response="Preciso de ajuda";
                                        break;
                                    case (byte)0x01:
                                        response="Preciso afastar-me";
                                        break;
                                    case (byte)0x02:
                                        response="Camião com problemas";
                                        break;
                                    case (byte)0x03:
                                        response="Preciso de suporte aéreo";
                                        break;
                                    case (byte)0x04:
                                        response="Vai descansar";
                                        break;
                                    case (byte)0x05:
                                        response="Suporte Aéreo a chegar";
                                        break;
                                    case (byte)0x06:
                                        response="Fogo a espalhar-se";
                                        break;
                                    case (byte)0x07:
                                        response="A retirar-me";
                                        break;
                                    case (byte)0x08:
                                        response="Fogo perto de casa";
                                        break;
                                    case (byte)0x09:
                                        response="Casa queimada";
                                        break;
                                    default:
                                        response="Erro";
                                        break;
                                }

                                if (In_Combate_Mode){
                                    playAudioMessages(Pred_Msg_Type);
                                }

                                Pred_Msg_Received=true;
                                break;

                            case cc_requests_fl_update_msg_type:
                                MY_NOTIFICATION_ID=4;
                                response="Actualizar Linha de Fogo";
                                fireline_update_request=true;
                                In_Fire_Line_Update=false;

                                break;

                            case cc_automatic_ack_msg_type:
                                MY_NOTIFICATION_ID=5;
                                response="Alerta Recebido pelo CC";
                                break;

                            case cc_requests_movetogps_msg_type:
                                compass_request=true;
                                In_Compass=false;
                                MY_NOTIFICATION_ID=6;
                                response="Move to GPS";

                                byte[] latitude =  Arrays.copyOfRange(pck_received.getMessage(), 0, 4);
                                byte[] longitude =  Arrays.copyOfRange(pck_received.getMessage(), 4, pck_received.getMessage().length);

                                ByteBuffer lat_bb = ByteBuffer.wrap(latitude);
                                lat_bb.order(ByteOrder.LITTLE_ENDIAN);
                                lat = lat_bb.getFloat();

                                ByteBuffer lon_bb = ByteBuffer.wrap(longitude);
                                lon_bb.order(ByteOrder.LITTLE_ENDIAN);
                                lon = lon_bb.getFloat();

                                break;

                            default:
                                break;
                        }

                        if(response!=null) {
                            Log.d("response", response);
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
                                    //countDownTimer_pred_msg.start();
                                }else
                                if (MY_NOTIFICATION_ID==3){
                                    notification_pred();
                                    //countDownTimer_pred_msg.start();
                                }
                                else{
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
                            }
                        }
                    }
                } else {

                    Log.w("CLientSocket", "SOCKET IS NOT CONNECTED::::" + isSocketAlive);
                    throw new UnknownHostException();
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

        //
        Intent intent_delete = new Intent(this, MyBroadcastReceiver_gps.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent_delete, 0);

        //
        Intent intent_ack = new Intent(this, MyBroadcastReceiver_gps_ack.class);
        PendingIntent pendingIntent_ack = PendingIntent.getBroadcast(this, 0, intent_ack, 0);


        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(resp)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pIntent)
                .setContentIntent(pendingIntent_ack)
                .setAutoCancel(true)
                .setDeleteIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
    }

    public void notification_pers(){

        Intent intent = new Intent(this, MyBroadcastReceiver_normal.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(response)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                        //.addAction(0, "Aceitar", pendingIntent)
                        //.setDeleteIntent(pendingIntent)
                .build();
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
    }

    public void notification_pred(){

        Intent intent = new Intent(this, MyBroadcastReceiver_normal_2.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(response)
                .setSmallIcon(R.drawable.droidbeiro_app_icon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                        //.addAction(0, "Aceitar", pendingIntent)
                        //.setDeleteIntent(pendingIntent)
                .build();
        myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // If you want to hide the notification after it was selected, do the code below
        notificationManager.notify(MY_NOTIFICATION_ID, mNotification);
    }


    public static class MyBroadcastReceiver extends BroadcastReceiver{

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

    public static class MyBroadcastReceiver_gps extends BroadcastReceiver{

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


    public static class MyBroadcastReceiver_gps_ack extends BroadcastReceiver{

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

            // cancel_CountDownTimer_pers_msg();
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

            //cancel_CountDownTimer_pred_msg();
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
        }
    }
}