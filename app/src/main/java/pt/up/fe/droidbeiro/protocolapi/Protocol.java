package pt.up.fe.droidbeiro.protocolapi;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Luis Ungaro
 */

public class Protocol {

    private final boolean machine;
    /*Server socket's port in order for the protocol to open SocketPro and
    Socket App*/
    private final int port;
    private final byte system_id;

    //Just to know when to stop
    private boolean running;

    private Socket SocketPro;
    private Socket SocketApp;
    private ObjectInputStream AppIn;
    private ObjectInputStream ProIn;
    private ObjectOutputStream AppOut;
    private ObjectOutputStream ProOut;


    public Protocol(boolean machine, int port, byte system_id){
        this.machine = machine;
        this.port = port;
        this.system_id = system_id;
        this.running = true;

    };

    public void execute() throws InterruptedException{

        byte id;
        byte spec;
        byte[] packet;

        rqst request;
        rspns response;


        /*Open the sockets
        172.30.71.108 is the current machine's address*/
        try {
            SocketPro = new Socket("172.30.71.108", port);
        } catch (IOException ex) {
            System.out.println("Protocol: Could not open SocketPro!\n");
            return;
        }

        try {
            SocketApp = new Socket("172.30.71.108", port);
        } catch (IOException ex) {
            System.out.println("Protocol: Could not open SocketApp!\n");
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

            //Protocol waits for an application's request
            request = (rqst)AppIn.readObject();
            System.out.println("Protocol: App requested id=" + request.id + " spec=" + request.spec +
                    " packet=" + Arrays.toString(request.packet));


            /*Protocol replies (in this action the reply id is not relevant,
            nevertheless it must be written to the objectStream)*/
            response = new rspns((byte)0x00);
            System.out.println("Protocol: Protocol replied id=" + response.id);
            AppOut.writeObject(response);


            /*Now that the protocol has replied (with an ack) it can
            do what the application asked, which is to unpack the packet and then
            decide what to do with it.
            Let's supose the protocol found that the packet's destination is not
            the current machine. Therefore, the protocol processes the packet and
            asks the application to send it to the next node.
            Since this action is a new one and it is a request from the protocol,
            it must be sent through the SocketPro (ProIn and ProOut objectStreams)
            */

            //Simulate processing time
            TimeUnit.MILLISECONDS.sleep(10);

            //Protocol creates a new rqst

            //Id of this action
            id = (byte)0x11;

            //Spec of the radio output
            spec = (byte)0x11;

            //Processed packet
            packet = new byte[] {(byte)0x04, (byte)0x03, (byte)0x02, (byte)0x01};

            request = new rqst(id, spec, packet);

            //Protocol sends request
            System.out.println("\nProtocol asks application to send a message:");
            System.out.println("Protocol: Protocol requested id=" + request.id + " spec=" + request.spec
                    + " packet=" + Arrays.toString(request.packet));
            ProOut.writeObject(request);

            /*Protocol waits for response, which in this case is relevant, since
            there is a chance of something going wrong*/
            response = (rspns)ProIn.readObject();
            System.out.println("Protocol: App replied id=" + response.id );

            /*And the procces repeats itself, without a known end. Therefore for
            demonstration purposes a loop is here to keep the protocol
            running until the application kills it.*/
            while(running);

        } catch (IOException ex) {
            System.out.println("Protocol: Could not send rqst or receive rspns!\n");
        } catch (ClassNotFoundException ex) {
            System.out.println("Protocol: No rspns object found in the socket!\n");
        }
    }

    public void Stop(){
        running = false;
    }
}