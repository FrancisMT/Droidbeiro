package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.TapDetection;

public class BombeiroMC extends Activity {

    private Button btn_sair_modo_combate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bombeiro_mc);

        btn_sair_modo_combate=(Button)findViewById(R.id.btn_modo_combate);

        btn_sair_modo_combate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Used to test
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BombeiroMC.this);
                //Setting Dialog Title
                alertDialog.setTitle("Modo de Combate");
                //Setting Dialog Message
                alertDialog.setMessage("Sair de Combate?");

                //Setting Positive "Sim" Button
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int which) {

                        // Write your code here to invoke SIM event

                        //Stop TapDetection Service
                        stopService(new Intent(BombeiroMC.this, TapDetection.class));

                        //Start NewActivity.class
                        Intent myIntent = new Intent(BombeiroMC.this,
                                BombeiroMain.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }
                });

                // Setting Negative "NÃO" Button
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NÃO event
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
            }
        });

        //Start TapDetection Service
        startService(new Intent(this, TapDetection.class));

    }

    @Override
    public void onBackPressed() {
        //Bombeiro apenas pode sair deste menu através do botão desennhado para o efeito
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bombeiro_mc, menu);
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
