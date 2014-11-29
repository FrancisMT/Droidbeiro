package pt.up.fe.droidbeiro.Protocol_API;

import java.io.Serializable;

/**
 *
 * @author Luis Ungaro
 */

/*These class must be Serializable!*/
public class rqst implements Serializable {

    public final byte id;           //identification character of the request
    public final byte spec;         //parameter of the request
    //public final boolean priority;  //true when the packet has high priority
    public final byte[] packet;     //packet to be processed --> Obeject Message_Struture

    public rqst(byte id, byte spec,/*boolean priority,*/ byte[] packet){
        this.id = id;
        this.spec = spec;
        //this.priority=priority;
        this.packet = packet;
    }
}