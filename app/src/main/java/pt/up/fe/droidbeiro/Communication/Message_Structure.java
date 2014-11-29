package pt.up.fe.droidbeiro.Communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Francisco on 25/11/2014.
 */
public class Message_Structure {

    private byte message_type;
    private byte firefighter_ID;
    private byte[] message;

    public Message_Structure(byte msg_type, byte ff_ID, byte[] msg){
        this.message_type=msg_type;
        this.firefighter_ID=ff_ID;
        this.message=msg;
    }

    public byte[] Message_to_Send(byte msg_type, byte ff_ID, byte[] msg) throws IOException {

        byte[] message_to_send;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(msg_type);
        outputStream.write(ff_ID);
        outputStream.write(msg);
        message_to_send = outputStream.toByteArray();

        return message_to_send;
    }

    public void Received_Message(byte[] msg){
        //byter parser

        this.message_type = (Arrays.copyOfRange(msg,0,1))[0];
        this.firefighter_ID = (Arrays.copyOfRange(msg,1,2))[0];
        this.message = Arrays.copyOfRange(msg,2,msg.length);
    }

}
