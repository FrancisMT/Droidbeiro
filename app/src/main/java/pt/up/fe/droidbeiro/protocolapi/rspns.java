package pt.up.fe.droidbeiro.protocolapi;


import java.io.Serializable;

/**
 *
 * @author Luis Ungaro
 */

/*These class must be Serializable!*/
public class rspns implements Serializable {
    public final byte id;


    public rspns(byte id){
        this.id = id;
    }
}