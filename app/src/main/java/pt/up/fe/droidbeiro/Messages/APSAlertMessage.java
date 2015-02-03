package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class APSAlertMessage {

    private byte MessageType = 18;
    private byte FireFighter_ID;

    Packet apsalert_packet;

    public APSAlertMessage (byte ff_id){
        this.FireFighter_ID = ff_id;
    }

    public void build_apsalert_packet() throws IOException {
        //Get Packet
        this.apsalert_packet = new Packet();
        //this.apsalert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getApsalert_packet() {
        return apsalert_packet;
    }
}
