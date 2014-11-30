package pt.up.fe.droidbeiro.Logic;

/**
 * Created by Francisco on 16/11/2014.
 */
public class User {

    private final byte firefighter_ID;

    public User(byte ID){
        this.firefighter_ID=ID;
    }

    public byte getFirefighter_ID(){
        return this.firefighter_ID;
    }

}
