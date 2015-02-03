package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 30/12/2014.
 */
public class ExitAlertMessage {

    private byte MessageType = 23;
    private byte FireFighter_ID;

    Packet exitalert_packet;

    public ExitAlertMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_exitalert_packet() throws IOException {
        //Get Packet
        this.exitalert_packet = new Packet();
        //this.exitalert_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getExitalert_packet() {
        return exitalert_packet;
    }
}
