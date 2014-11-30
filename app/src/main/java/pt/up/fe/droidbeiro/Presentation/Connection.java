package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

//import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.R;
//import pt.up.fe.droidbeiro.Communication.Client_Connection;

public class Connection extends Activity {

    private Button btn_ligar;
    private EditText ip_address_field;
    private EditText porta_field;

    private static String SERVER_PORT;
    private static String SERVER_IP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        // Hiding the action bar
        getActionBar().hide();

        btn_ligar = (Button)findViewById(R.id.btn_ligar);
        ip_address_field = (EditText)findViewById(R.id.ip_address_field);
        porta_field = (EditText)findViewById(R.id.porta_field);

        btn_ligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((ip_address_field.getText().toString().trim().length() > 0) && (porta_field.getText().toString().trim().length() > 0)){

                    SERVER_IP=ip_address_field.getText().toString().trim();
                    SERVER_PORT= porta_field.getText().toString();

                    Intent Connection = new Intent(Connection.this, Client_Socket.class);
                    Connection.putExtra("IP",SERVER_IP);
                    Connection.putExtra("PORT",SERVER_PORT);
                    startService(Connection);

                    Intent intent = new Intent(Connection.this, Login.class);
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(), "Por favor introduza o par IP/Porta", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.menu_connection, menu);
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

    public void new_Client_Config_file(String SERVER_IP, int SERVER_PORT) throws IOException {

        String nameOfFile = "Client_Config.txt";
        String file_content = SERVER_IP + ">" + Integer.toString(SERVER_PORT);

        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "Droidbeiro_config");
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            try {
                File file = new File(newFolder, nameOfFile);
                file.createNewFile();
            } catch (Exception ex) {
                System.out.println("Cannot open folder " + ex);
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }

        FileOutputStream fosAppend;

        fosAppend = openFileOutput(nameOfFile, Context.MODE_APPEND);
        fosAppend.write(Integer.parseInt(file_content));
        fosAppend.flush();
        fosAppend.close();

    }
}
