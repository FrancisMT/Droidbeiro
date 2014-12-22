package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class SurroundedByFlamesMessage {

    private byte MessageType = 12;
    private byte FireFighter_ID;

    Packet surroundedbyflames_packet;

    public SurroundedByFlamesMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_sos_packet() throws IOException {
        //Get Packet
        this.surroundedbyflames_packet = new Packet();
        this.surroundedbyflames_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getSurroundedbyflames_packet() {
        return surroundedbyflames_packet;
    }
}
