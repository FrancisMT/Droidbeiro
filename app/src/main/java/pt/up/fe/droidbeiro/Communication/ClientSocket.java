package pt.up.fe.droidbeiro.Communication;

import java.io.*;
import java.net.*;

import android.util.Log;

/**
 * Created by Francisco on 16/11/2014.
 */
public class ClientSocket extends Thread{

    private Socket cSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private int isSocketAlive = 0;

    private static String SERVER_IP;// = "172.30.45.224";
    private static int SERVER_PORT;// = 80;
    private static final int SERVER_TIMEOUT = 1000;

    private boolean dataToSend = false;
    private boolean dataToRead = false;
    private String dataSend = null;
    private String dataRead = null;

    public void run(){
        try{
            this.cSocket = new Socket();
            cSocket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), SERVER_TIMEOUT);

            if (cSocket.isConnected()){

                this.isSocketAlive = 1;
                Log.w("CLientSocket", "SOCKET ISCONNECTED::::" + isSocketAlive);

                this.out = new PrintWriter(cSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

                while (isSocketAlive==1){

                    if(dataToSend){
                        this.out.println(dataSend);
                        this.dataToSend = false;

                        while( (this.dataRead=in.readLine())==null){}
                        this.dataToRead = true;
                    }
                }
                this.out.close();
                this.in.close();
                this.cSocket.close();
            }else {
                this.isSocketAlive=2;
                Log.w("CLientSocket", "SOCKET IS NOT CONNECTED::::" + isSocketAlive);
                throw new UnknownHostException();
            }

        }catch (UnknownHostException e) {
            Log.e("ClientSocket", "Host desconhecido!!!");
            e.printStackTrace();
            this.isSocketAlive=2;
        } catch (ConnectException e2) {
            Log.e("ClientSocket", "Impossivel conectar ao servidor!!!");
            e2.printStackTrace();
            this.isSocketAlive=2;
        } catch (IOException e1) {
            Log.e("ClientSocket", "IOException...!!!");
            e1.printStackTrace();
            this.isSocketAlive=2;
        }
    }

    public void setSERVER_IP(String ip_address){
        this.SERVER_IP=ip_address;
    }

    public void setSERVER_PORT(int port_number){
        this.SERVER_PORT=port_number;
    }

    public void send(String s){
        this.dataSend = s;
        this.dataToSend = true;
    }

    public String read(){
        if(dataToRead)
            return dataRead;
        else
            return null;
    }

    public int IsSocketAlive(){
        return isSocketAlive;
    }

    public void close(){
        this.isSocketAlive=2;
    }







}
