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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.LogoutMessage;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.Acelarometro;
import pt.up.fe.droidbeiro.Service.Bussola;
import pt.up.fe.droidbeiro.Service.GPS;

public class ChefeMain extends Activity {

    // Initialize the array
    String[] messages = {   "Preciso de ajuda",
                            "Preciso afastar-me",
                            "Camião com problemas",
                            "Preciso de suporte aéreo",
                            "Fogo a espalhar-se",
                            "A retirar-me",
                            "Fogo perto de casa",
                            "Casa queimada",
                        };

    // Declare the UI components
    private ListView lista_mensagens_layout;
    private ArrayAdapter arrayAdapter;
    private Button btn_enviar_msg;
    private Button btn_linha_fogo;
    private String mensagem;
    private EditText custom_message;

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
        bindService(new Intent(ChefeMain.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
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
        setContentView(R.layout.activity_chefe_main);
        final Intent intentService = new Intent(this, GPS.class);
        startService(intentService);

        //start service on create
        doBindService();

        lista_mensagens_layout = (ListView) findViewById(R.id.lista_mensagens);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        lista_mensagens_layout.setAdapter(arrayAdapter);

        custom_message=(EditText)findViewById(R.id.msg_custom);
        btn_enviar_msg = (Button)findViewById(R.id.btn_enviar_dst);
        btn_linha_fogo= (Button)findViewById(R.id.btn_linha_fogo);

        lista_mensagens_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {//    if (!msg_type) {
                custom_message.setText("");
                mensagem = lista_mensagens_layout.getItemAtPosition(position).toString();
                custom_message.setText(mensagem);
            }
        });

        btn_enviar_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mensagem = custom_message.getText().toString().trim();


                if (!(mensagem.isEmpty())) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeMain.this);
                    alertDialog.setTitle("Enviar mensagem ?");
                    alertDialog.setMessage(mensagem);

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //envia a mensagem para o centro de controlo

                            custom_message.setText("");
                        }
                    });

                    // Setting Negative "NÃO" Button
                    alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //envia a mensagem para o centro de controlo
                            custom_message.setText("");
                        }
                    });
                    alertDialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Nada a enviar", Toast.LENGTH_LONG).show();
                }

            }
        });

        btn_linha_fogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start NewActivity.class
                Intent myIntent = new Intent(ChefeMain.this,
                        ChefeLF.class);
                startActivity(myIntent);
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
        getMenuInflater().inflate(R.menu.menu_chefe_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.login:

                /****************************************************************/
                LogoutMessage logout_msg = new LogoutMessage(CS.getFirefighter_ID());
                try {
                    logout_msg.build_logout_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(logout_msg.getLogout_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doUnbindService();
                /****************************************************************/

                Intent login_Intent = new Intent(ChefeMain.this, Login.class);
                login_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login_Intent);

                return true;
            case R.id.connection:

                /****************************************************************/
                LogoutMessage logout_msg_ = new LogoutMessage(CS.getFirefighter_ID());
                try {
                    logout_msg_.build_logout_packet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    CS.send_packet(logout_msg_.getLogout_packet());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doUnbindService();
                try {
                    CS.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /****************************************************************/

                Intent connection_Intent = new Intent(ChefeMain.this, Connection.class);
                connection_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(connection_Intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
        }

    }
