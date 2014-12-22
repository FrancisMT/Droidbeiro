package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class PersonalizedMessage {

    private byte MessageType = 11;
    private byte FireFighter_ID;

    private String personalized_message = "";

    Packet personalizedmessage_packet;

    public PersonalizedMessage(byte ff_id, String persn_msg){
        this.FireFighter_ID=ff_id;
        this.personalized_message=persn_msg;
    }

    public void build_personalizedmessage_packet() throws IOException {

        //Get Message Content
        byte[] message_content = personalized_message.getBytes("ISO-8859-1");

        //Get Packet
        this.personalizedmessage_packet = new Packet();
        this.personalizedmessage_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getPersonalizedmessage_packet() {
        return personalizedmessage_packet;
    }
}
