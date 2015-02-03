package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class SOSMessage {

    private byte MessageType = 12;
    private byte FireFighter_ID;

    Packet sos_packet;

    public SOSMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_sos_packet() throws IOException {
        //Get Packet
        this.sos_packet = new Packet();
        //this.sos_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getSos_packet() {
        return sos_packet;
    }
}
