package pt.up.fe.droidbeiro.Logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Francisco on 25/11/2014.
 */
public class PacketContent {

    private byte message_type;
    private byte firefighter_ID;
    private byte[] message;

    public PacketContent(byte msg_type, byte ff_ID, byte[] msg){
        this.message_type=msg_type;
        this.firefighter_ID=ff_ID;
        this.message=msg;
    }

    public byte[] Packet_to_Send(byte msg_type, byte ff_ID, byte[] msg) throws IOException {

        byte[] packet_to_send;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(msg_type);
        outputStream.write(ff_ID);
        outputStream.write(msg);
        packet_to_send = outputStream.toByteArray();

        return packet_to_send;
    }

    public void Received_Packet(byte[] msg){

        this.message_type = (Arrays.copyOfRange(msg,0,1))[0];
        this.firefighter_ID = (Arrays.copyOfRange(msg,1,2))[0];
        this.message = Arrays.copyOfRange(msg,2,msg.length);
    }

}
