package pt.up.fe.droidbeiro.Logic;

/**
 * Created by Francisco on 29/11/2014.
 */
public class Packet {

    /**
     * True -> Message should be sent to the protocol
     * False -> Message should be directly sent to the protocol
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
}
