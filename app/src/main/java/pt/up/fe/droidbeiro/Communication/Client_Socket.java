package pt.up.fe.droidbeiro.Communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

    /***********************************************************/


    private Socket cSocket = null;
    //private PrintWriter out = null;
    //private BufferedReader in = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

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

    private static final int MY_NOTIFICATION_ID=1;
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

    public void send_packet(Packet pck_to_send) throws IOException {
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
                        response=null;

                        switch(pck_received.getMessageType()){

                            case prelogin_msg_type:
                                Firefighter_ID=pck_received.getFirefighterID();

                                response="Ligado ao Centro de Controlo";

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
                                response="Actualizar Linha de Fogo";
                                break;

                            case cc_automatic_ack_msg_type:
                                response="Alerta Recebido";
                                break;

                            default:
                                break;
                        }

                        if(response!=null) {
                            Log.d("response", response);
                            if (!In_Combate_Mode) {
                                // Send Notification
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


}