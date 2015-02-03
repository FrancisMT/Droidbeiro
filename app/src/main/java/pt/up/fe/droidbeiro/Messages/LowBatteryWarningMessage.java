package pt.up.fe.droidbeiro.Messages;

import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class LowBatteryWarningMessage {

    private byte MessageType = 21;
    private byte FireFighter_ID;

    Packet lowbatterywarning_packet;

    public LowBatteryWarningMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_LowBatteryWarning_packet() throws IOException {
        //Get Packet
        this.lowbatterywarning_packet = new Packet();
        //this.lowbatterywarning_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getLowbatterywarning_packet() {
        return lowbatterywarning_packet;
    }
}
