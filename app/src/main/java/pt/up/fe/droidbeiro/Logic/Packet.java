package pt.up.fe.droidbeiro.Logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Francisco on 29/11/2014.
 */
public class Packet {

    /**
     * True -> Message should be sent to the protocol
     * False -> Message should be directly read by the app
     */
    public boolean hasProtocolHeader;
    public byte[] packetContent;

    public Packet(boolean hasPTCHeader, byte[] pktContent){
        this.hasProtocolHeader=hasPTCHeader;
        this.packetContent=pktContent;
    }

    public boolean getHasProtocolHeader(){
        return this.hasProtocolHeader;
    }

    public byte[] getPacketContent(){
        return this.packetContent;
    }

    public byte[] getMessage(){
        return Arrays.copyOfRange(packetContent,2,packetContent.length);
    }

}
