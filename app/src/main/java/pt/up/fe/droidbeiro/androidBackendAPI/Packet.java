package pt.up.fe.droidbeiro.androidBackendAPI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Francisco on 29/11/2014.
 */
public class Packet implements Serializable{

    /**
     * True -> Message should be sent to the protocol
     * False -> Message should be directly read by the app
     */
    public boolean hasProtocolHeader;
    public byte[] packetContent;

    public Packet(){
        this.hasProtocolHeader=false;
        this.packetContent=null;
    }

    public boolean getHasProtocolHeader(){
        return this.hasProtocolHeader;
    }

    public byte[] getPacketContent(){
        return this.packetContent;
    }

    public byte getMessageType(){
        return packetContent[0];
    }

    public byte getFirefighterID(){
        return packetContent[1];
    }

    public byte[] getMessage(){
        return Arrays.copyOfRange(packetContent,2,packetContent.length);
    }

    public void build_packet(boolean hasPtrclHeader, byte message_type, byte ff_id, byte[] message) throws IOException {

        ByteArrayOutputStream packet_content = new ByteArrayOutputStream();
        packet_content.write(message_type);
        packet_content.write(ff_id);
        if (message!=null) {
            try {
                packet_content.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.hasProtocolHeader=hasPtrclHeader;
        this.packetContent= packet_content.toByteArray();
    }

}
