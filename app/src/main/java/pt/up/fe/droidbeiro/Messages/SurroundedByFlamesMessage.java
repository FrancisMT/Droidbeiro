package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class SurroundedByFlamesMessage {

    private int msg_type=13;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet surroundedbyflames_packet;

    public SurroundedByFlamesMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_sos_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.surroundedbyflames_packet = new Packet();
        this.surroundedbyflames_packet.hasProtocolHeader=true;
        this.surroundedbyflames_packet.packetContent=packet_content_final.toByteArray();
        //this.surroundedbyflames_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getSurroundedbyflames_packet() {
        return surroundedbyflames_packet;
    }
}
