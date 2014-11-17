package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import pt.up.fe.droidbeiro.R;

public class ChefeLF extends Activity {

    // Initialize the array
    String[] distancias = {   "10","20","30","40","50","60","70","80","90","100",
                            "110","120","130","140","150","160","170","180","190","200",
                            "210","220","230","240","250","260","270","280","290","300",
                            "310","320","330","340","350","360","370","380","390","400",
                            "410","420","430","440","450","460","470","480","490","500",
                            "510","520","530","540","550","560","570","580","590","600",
                            "610","620","630","640","650","660","670","680","690","700",
                            "810","820","830","840","850","860","870","880","890","900",
                            "910","920","930","940","950","960","970","980","990","1000"
                        };

    private ListView lista_distancias_layout;
    private ArrayAdapter arrayAdapter;
    private Button btn_enviar_dst;
    private String distancia;
    private EditText custom_dst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chefe_lf);

        lista_distancias_layout = (ListView) findViewById(R.id.lista_dst);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, distancias);
        lista_distancias_layout.setAdapter(arrayAdapter);

        custom_dst=(EditText)findViewById(R.id.dst_custom);
        btn_enviar_dst = (Button)findViewById(R.id.btn_enviar_dst);

        lista_distancias_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {//    if (!msg_type) {
                custom_dst.setText("");
                distancia = lista_distancias_layout.getItemAtPosition(position).toString();
                custom_dst.setText(distancia);
            }
        });

        btn_enviar_dst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                distancia = custom_dst.getText().toString().trim();


                if (!(distancia.isEmpty())) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                    alertDialog.setTitle("Enviar distância ?");
                    alertDialog.setMessage(distancia);

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //envia a mensagem para o centro de controlo
                            custom_dst.setText("");
                        }
                    });

                    // Setting Negative "NÃO" Button
                    alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //envia a mensagem para o centro de controlo
                            custom_dst.setText("");
                        }
                    });
                    alertDialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Nada a enviar", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        //Start NewActivity.class
        Intent myIntent = new Intent(ChefeLF.this,ChefeMain.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chefe_l, menu);
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
