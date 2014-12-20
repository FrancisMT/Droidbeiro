package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class LogoutMessage {

    private byte MessageType = 8;
    private byte FireFighter_ID;

    Packet logout_packet;

    public LogoutMessage(byte ff_id){
        this.FireFighter_ID = ff_id;
    }

    public void build_logout_packet() throws IOException {
        //Get Packet
        this.logout_packet = new Packet();
        this.logout_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getLogout_packet() {
        return logout_packet;
    }
}
