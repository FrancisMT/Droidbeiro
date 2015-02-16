package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class AcceptRequestMessage {

    private int msg_type=19;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet acceptrequest_packet;

    public AcceptRequestMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_acceptrequest_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.acceptrequest_packet = new Packet();
        this.acceptrequest_packet.hasProtocolHeader=true;
        this.acceptrequest_packet.packetContent=packet_content_final.toByteArray();

        //this.acceptrequest_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getAcceptrequest_packet() {
        return acceptrequest_packet;
    }
}
