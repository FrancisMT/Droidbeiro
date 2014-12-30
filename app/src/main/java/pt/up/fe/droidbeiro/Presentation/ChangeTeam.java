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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Logic.User;
import pt.up.fe.droidbeiro.Messages.UpdateTeamMessage;
import pt.up.fe.droidbeiro.R;

public class ChangeTeam extends Activity {

    private Button btn_enviar_equipa;
    private EditText equipa_field;

    private static int equipa=0;

    Client_Socket CS = null;
    boolean CSisBound;

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
        bindService(new Intent(ChangeTeam.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
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
        setContentView(R.layout.activity_change_team);

        // Hiding the action bar
        getActionBar().hide();

        //start service on create
        doBindService();

        btn_enviar_equipa = (Button)findViewById(R.id.btn_enviar_equipa);
        equipa_field = (EditText)findViewById(R.id.equipa);

        btn_enviar_equipa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (equipa_field.getText().toString().trim().length()>0){

                    equipa = Integer.valueOf(equipa_field.getText().toString());

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChangeTeam.this);
                    alertDialog.setTitle("Mudar de Equipa");
                    alertDialog.setMessage("Equipa " + equipa + "?");

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            UpdateTeamMessage upte_msg = new UpdateTeamMessage(CS.getFirefighter_ID(), equipa);
                            try {
                                upte_msg.build_updateteam_packet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                CS.send_packet(upte_msg.getUpdateteam_packet());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            equipa_field.setText("");
                        }
                    });

                    // Setting Negative "NÃO" Button
                    alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Por favor introduza o ID da equipa", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_team, menu);
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
}
