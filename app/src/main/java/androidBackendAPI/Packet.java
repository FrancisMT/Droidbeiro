package androidBackendAPI;

import java.io.Serializable;

public class Packet implements Serializable{

    public boolean hasProtocolHeader;
    public byte[] packetContent;
    private static final long serialVersionUID = 1L;
}
