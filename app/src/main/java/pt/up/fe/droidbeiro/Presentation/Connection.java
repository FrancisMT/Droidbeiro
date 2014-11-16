package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pt.up.fe.droidbeiro.R;

public class Connection extends Activity {

    private Button btn_ligar;
    private EditText ip_address_field;
    private EditText porta_field;

    public static int SERVER_PORT;
    public static String SERVER_IP;

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
                    SERVER_PORT= Integer.parseInt(porta_field.getText().toString());


                    Intent intent = new Intent(Connection.this, Login.class);
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(), "Por favor introduza o par IP/Porta", Toast.LENGTH_LONG).show();
                }

            }
        });
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
}
