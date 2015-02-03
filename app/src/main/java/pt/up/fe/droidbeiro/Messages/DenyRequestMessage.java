package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class DenyRequestMessage {

    private byte MessageType = 20;
    private byte FireFighter_ID;

    Packet denyrequest_packet;

    public DenyRequestMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_denyrequest_packet() throws IOException {
        //Get Packet
        this.denyrequest_packet = new Packet();
        //this.denyrequest_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getDenyrequest_packet() {
        return denyrequest_packet;
    }
}
