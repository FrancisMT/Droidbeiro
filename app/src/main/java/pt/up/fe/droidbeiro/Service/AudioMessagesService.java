package pt.up.fe.droidbeiro.Service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Service for playing audio messages. Automatically plays audio message when called.
 *
 *  messageID - ID of the message (0 - 9)
 *
 *
 * Pre-Defined Messages:
 * 0 Need Support
 * 1 Need to Back Down
 * 2 Firetruck is in Trouble
 * 3 Need Aerial Support
 * 4 Go Rest
 * 5 Aerial Support Incoming
 * 6 The Fire is Spreading
 * 7 We’re Leaving
 * 8 Fire Getting Close to House
 * 9 House Burned
 *
 * Receives messageID as string from bundle.
 * Example of how to call this AudioMessageService from an activity bellow:
 *
 *      String ID = String.valueOf(MessageID); //MessageID = ID(0-9) of desired message
 *      Intent intent = new Intent(this,AudioMessagesService.class);
 *      Bundle bundle = new Bundle();
 *      bundle.putString("ID",ID);
 *      intent.putExtras(bundle);
 *
 */

public class AudioMessagesService extends Service {


    public static int messageID;

    //Paths
    /*public static String m_0 = "m0_help";
    public static String m_1 = "m1_back_down";
    public static String m_2 = "m2_firetruck";
    public static String m_3 = "m3_aerial";
    public static String m_4 = "m4_rest";
    public static String m_5 = "m5_aerial_coming";
    public static String m_6 = "m6_fire_spreading";
    public static String m_7 = "m7_leaving";
    public static String m_8 = "m8_close_to_house";
    public static String m_9 = "m9_house_burned";*/

    public static String m_0 = "m0_afirmativo";
    public static String m_1 = "m1_aguarde";
    public static String m_2 = "m2_assim_farei";
    public static String m_3 = "m3_correto";
    public static String m_4 = "m4_errado";
    public static String m_5 = "m5_informe";
    public static String m_6 = "m6_negativo";
    public static String m_7 = "m7_a_caminho";
    public static String m_8 = "m8_no_local";
    public static String m_9 = "m9_no_hospital";
    public static String m_10 = "m10_disponivel";
    public static String m_11 = "m11_de_regresso";
    public static String m_12 = "m12_inop";
    public static String m_13 = "m13_no_quartel";
    public static String m_14 = "m14_necessito_de_reforcos";
    public static String m_15 = "m15_casa_em_perigo";
    public static String m_16 = "m1_back_down";
    public static String m_17 = "m2_firetruck";
    public static String m_18 = "m18_descanse";
    public static String m_19 = "m19_fogo_a_alastrar";

    ArrayList<String> audioMessagesList = new ArrayList<String>(20);

    CountDownTimer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.e("On Service", "Audio Messages");


        //Add messages to list
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
        audioMessagesList.add(10,m_10);
        audioMessagesList.add(11,m_11);
        audioMessagesList.add(12,m_12);
        audioMessagesList.add(13,m_13);
        audioMessagesList.add(14,m_14);
        audioMessagesList.add(15,m_15);
        audioMessagesList.add(16,m_16);
        audioMessagesList.add(17,m_17);
        audioMessagesList.add(18,m_18);
        audioMessagesList.add(19,m_19);
        //timer para verificar se ja passaram 30seg
        /*timer = new CountDownTimer(30000,50000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

            }
        };*/
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Bundle bundle = intent.getExtras();
        messageID = Integer.parseInt(bundle.getString("ID"));
        playAudioMessages();
        return messageID;
    }


    private void playAudioMessages() {

        String path = "android.resource://"+getPackageName()+"/raw/"+audioMessagesList.get(messageID);

        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
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
