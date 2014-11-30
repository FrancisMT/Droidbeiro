package pt.up.fe.droidbeiro.Protocol_API;


import java.io.Serializable;

/**
 *
 * @author Luis Ungaro
 */

/*These class must be Serializable!*/
public class rspns implements Serializable {

    public final byte id;       //identification character of the response

    public rspns(byte id){
        this.id = id;
    }
}