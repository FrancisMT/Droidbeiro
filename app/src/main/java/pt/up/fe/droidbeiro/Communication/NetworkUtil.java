package pt.up.fe.droidbeiro.Communication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Francisco on 07/01/2015.
 */

public class NetworkUtil {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {

        Client_Socket CS = Client_Socket.getInstance();

        int conn = NetworkUtil.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtil.TYPE_WIFI) {
            Log.e("Connection Status:", "Ligação Wifi estabelecida");
            CS.GSM_Status=true;
            CS.running=true;
            CS.GSM_Status_Changed=true;
            status = "Ligação Wifi estabelecida";
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = "Ligação de dados estabelecida";
            CS.GSM_Status=true;
            CS.running=true;
            CS.GSM_Status_Changed=true;
            Log.e("Connection Status:","Ligação de dados estabelecida");
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "Ligação perdida";
            CS.GSM_Status=false;
            CS.running=false;
            CS.GSM_Status_Changed=true;
            Log.e("Connection Status:","Ligação perdida");
        }
        return status;
    }
}