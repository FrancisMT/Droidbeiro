package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Logic.User;
import pt.up.fe.droidbeiro.Messages.LoginMessage;
import pt.up.fe.droidbeiro.Messages.MD5;
import pt.up.fe.droidbeiro.Messages.SOSMessage;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.androidBackendAPI.Packet;

public class Login extends Activity {

    private Button btn_entrar;
    private EditText username_field;
    private EditText password_field;

    private static String username;
    private static String password;

    Client_Socket CS = null;
    boolean CSisBound;

    public static User user;


    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            CS = ((Client_Socket.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            CS = null;
        }
    };

    private void doBindService() {
        bindService(new Intent(Login.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
        CSisBound = true;
        if(CS!=null){
            CS.IsBoundable();
        }
    }

    private void doUnbindService() {
        if (CSisBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            CSisBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hiding the action bar
        getActionBar().hide();

        //start service on create
        doBindService();

        btn_entrar = (Button)findViewById(R.id.btn_entrar);
        username_field = (EditText)findViewById(R.id.username);
        password_field = (EditText)findViewById(R.id.password);

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((username_field.getText().toString().trim().length() > 0) && (password_field.getText().toString().trim().length() > 0)){

                    username=username_field.getText().toString().trim();
                    password=password_field.getText().toString().trim();

                    /****************************************************************/
                    LoginMessage login_msg = new LoginMessage((byte)0x01, username, password);
                    try {
                        login_msg.build_login_packet();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        CS.send_packet(login_msg.getLogin_packet());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("Response from server", CS.getMessage());
                    /****************************************************************/

                    //Used to test
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
                    //Setting Dialog Title
                    alertDialog.setTitle("Apenas para testes");
                    //Setting Dialog Message
                    alertDialog.setMessage("Bombeiro ou Chefe?");

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Chefe", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int which) {

                            // Write your code here to invoke SIM event
                            //Start NewActivity.class
                            Intent myIntent = new Intent(Login.this,
                                    ChefeMain.class);
                            startActivity(myIntent);
                        }
                    });

                    // Setting Negative "NÃO" Button
                    alertDialog.setNegativeButton("Bombeiro", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to invoke NÃO event
                            //Start NewActivity.class
                            Intent myIntent = new android.content.Intent(Login.this,
                                    BombeiroMain.class);
                            startActivity(myIntent);
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();

                }else{
                    Toast.makeText(getApplicationContext(), "Por favor introduza o par Utilizador/Password: " + CS.getFirefighter_ID(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_lock_power_off).setTitle("Sair")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Não", null).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
