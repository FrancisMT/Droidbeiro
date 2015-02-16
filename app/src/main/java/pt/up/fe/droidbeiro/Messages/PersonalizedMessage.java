package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class PersonalizedMessage {

    private int msg_type=11;
    private byte MessageType = (byte)msg_type;
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
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);
        if (message_content!=null) {
            try {
                packet_content_final.write(message_content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.personalizedmessage_packet = new Packet();
        this.personalizedmessage_packet.hasProtocolHeader=true;
        this.personalizedmessage_packet.packetContent=packet_content_final.toByteArray();
        //this.personalizedmessage_packet.build_packet(false, this.MessageType, this.FireFighter_ID, message_content);
    }

    public Packet getPersonalizedmessage_packet() {
        return personalizedmessage_packet;
    }
}
