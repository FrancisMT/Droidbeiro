package pt.up.fe.droidbeiro.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidBackendAPI.Packet;

/**
 * Created by Francisco on 19/12/2014.
 */
public class LowBatteryWarningMessage {

    private int msg_type=21;
    private byte MessageType = (byte)msg_type;
    private byte FireFighter_ID;

    Packet lowbatterywarning_packet;

    public LowBatteryWarningMessage(byte ff_id){
        this.FireFighter_ID=ff_id;
    }

    public void build_LowBatteryWarning_packet() throws IOException {
        //Get Packet
        ByteArrayOutputStream packet_content_final = new ByteArrayOutputStream();
        packet_content_final.write(this.MessageType);
        packet_content_final.write(this.FireFighter_ID);

        this.lowbatterywarning_packet = new Packet();
        this.lowbatterywarning_packet.hasProtocolHeader=true;
        this.lowbatterywarning_packet.packetContent=packet_content_final.toByteArray();
        //this.lowbatterywarning_packet.build_packet(false, this.MessageType, this.FireFighter_ID, null);
    }

    public Packet getLowbatterywarning_packet() {
        return lowbatterywarning_packet;
    }
}
