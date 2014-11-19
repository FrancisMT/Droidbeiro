package pt.up.fe.droidbeiro.Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import pt.up.fe.droidbeiro.protocolapi.*;

/**
 * Created by Francisco on 19/11/2014.
 */
public class Client_Connection extends Thread{
    private Socket SocketPro;
    private Socket SocketApp;
    private ObjectInputStream AppIn;
    private ObjectInputStream ProIn;
    private ObjectOutputStream AppOut;
    private ObjectOutputStream ProOut;

    Protocol Protocol_GX;

    @Override
    public void run(){

        final ServerSocket serverSocket;

        byte id;
        byte spec;
        byte[] packet;

        rqst request;
        rspns response;

        Thread ProThread;

        /*Create a server socket in order to establish connection with the
        protocol*/
        try {
            //0 means any available port
            serverSocket = new ServerSocket(0);
        } catch (IOException ex) {
            System.out.println("Could not find a free port!\n");
            return ;
        }


        /*Create a thread that runs the protocol. In the android app, this thread
        must do so inside a service*/
        ProThread = new Thread(){

            @Override
            public void run(){

                try {
                    Protocol_GX = new Protocol(true, serverSocket.getLocalPort(), (byte)0x00);
                    Protocol_GX.execute();
                } catch (InterruptedException ex) {
                    System.out.println("Thread stoped!");
                }

            }


        };

        ProThread.start();

        /*Create socket SocketPro for the protocol's requests and the application's
        responses*/
        try {
            SocketPro = serverSocket.accept();
        } catch (IOException ex) {
            System.out.println("Application: Could not open SocketPro!\n");
            return;
        }

        /*Create socket SocketApp for the application's requests and the protocol's
        responses*/
        try {
            SocketApp = serverSocket.accept();
        } catch (IOException ex) {
            System.out.println("Application: Could not open SocketApp!\n");
            return;
        }


        /*Transform socket streams into ObjectStreams in order to send objects
        instead of bytes (Out's must always be created first and flushed!)*/
        try {
            AppOut = new ObjectOutputStream(SocketApp.getOutputStream());
            ProOut = new ObjectOutputStream(SocketPro.getOutputStream());
            AppOut.flush();
            ProOut.flush();
            AppOut.reset();
            ProOut.reset();
            AppIn = new ObjectInputStream(SocketApp.getInputStream());
            ProIn = new ObjectInputStream(SocketPro.getInputStream());

        } catch (IOException ex) {
            System.out.println("Could not create Object Streams!\n");
            return;
        }


        /*Exchange random messages just for demostration purposes
        On the real project these messages would be either writen by the firefighter
        or received by bluetooth*/
        try {

            //Aplication receives a packet and asks the protocol to unpack it

            //Id of this Action
            id = (byte)0x00;

            //Socket ID if the current machine is the backend pc, irrelevant in
            //the android machine
            spec = (byte)0x11;

            //Received packet (here it is created one for demonstration)
            packet = new byte[] {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04};

            //rqst object
            request = new rqst(id, spec, packet);

            System.out.println("\nApplication asks protocol to unpack a message:");
            System.out.println("App: App requested id=" + request.id + " spec=" + request.spec
                    + " packet=" + Arrays.toString(request.packet));

            //Send request to protocol
            AppOut.writeObject(request);

            /*Protocol replies (in this action the reply id is not relevant,
            nevertheless it must be read from the objectStream!)*/
            response = (rspns)AppIn.readObject();
            System.out.println("App: protocol replied id=" + response.id);


            /*Now that the protocol has replied (with an ack) it can do what the
            application asked, which is to unpack the packet and then decide what
            to do with it.
            Let's supose the protocol found that the packet's destination is not
            the current machine. Therefore, the protocol processes the packet and
            asks the application to send it to the next node.
            Since this action is a new one and it is a request from the protocol,
            it must be sent through the SocketPro (ProIn and ProOut objectStreams)
            */

            //The Application waits for the protocol's request
            request = (rqst)ProIn.readObject();
            System.out.println("App: Protocol requested id=" + request.id + " spec=" + request.spec +
                    " packet=" + Arrays.toString(request.packet));

            /*Application sends reply, which in this case is relevant, since
            there is a chance of something going wrong*/
            response = new rspns((byte)0x00); //Everithing OK
            System.out.println("App: App replied id=" + response.id);
            ProOut.writeObject(response);

        } catch (IOException ex) {
            System.out.println("App: Could not send rqst or receive rspns!\n");
        } catch (ClassNotFoundException ex) {
            System.out.println("App: No rspns object found in the socket!\n");
        }

        /*Let's suposed that the operation ended (fire is out). Now the application
        must stop the thread running the protocol*/
        Protocol_GX.Stop();
    }


}
