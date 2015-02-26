package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import pt.up.fe.droidbeiro.Communication.Client_Socket;
import pt.up.fe.droidbeiro.Messages.AcceptRequestMessage;
import pt.up.fe.droidbeiro.Messages.ChangeFireLineStatusMessage;
import pt.up.fe.droidbeiro.Messages.FirelineMessage;
import pt.up.fe.droidbeiro.Messages.LogoutMessage;
import pt.up.fe.droidbeiro.Messages.ReplaceFireLineMessage;
import pt.up.fe.droidbeiro.R;
import pt.up.fe.droidbeiro.Service.Acelarometro;
import pt.up.fe.droidbeiro.Service.Bussola;
import pt.up.fe.droidbeiro.Service.GPS;

public class ChefeLF extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor acelarometro;
    public Float currentDegree;
    Sensor magnometro;
    float gravity0;
    float gravity1;
    float gravity2;
    float[] mGravity;
    float[] mGeomagnetic;
    ImageView image;
    TextView tvHeading;

    String[] distancias = { "5","10","15","20","25","30","35","40","45","50","55","60","65","70","75","80","85","90","95","100",
                            "105","110","115","120","125","130","135","140","145","150","155","160","165","170","175","180","185","190","195","200",
                            "205","210","215","220","225","230","235","240","245","250","255","260","265","270","275","280","285","290","295","300",
                            "305","310","315","320","325","330","335","340","345","350","355","360","635","370","375","380","385","390","395","400",
                            "405","410","415","420","425","430","435","440","445","450","455","460","465","470","475","480","485","490","495","500",
                            "505","510","515","520","525","530","535","540","545","550","555","560","565","570","575","580","585","590","595","600",
                            "605","610","615","620","625","630","635","640","645","650","655","660","665","670","675","680","685","690","695","700",
                            "705","710","715","720","725","730","735","740","745","750","755","760","765","770","775","780","785","790","795","800",
                            "805","810","815","820","825","830","835","840","845","850","855","860","865","870","875","880","885","890","895","900",
                            "905","910","915","920","925","930","935","940","945","950","955","960","965","970","975","980","985","990","995","1000"
                        };


    private ListView lista_distancias_layout;
    private ArrayAdapter arrayAdapter;
    private Button btn_enviar_dst;
    private Button btn_apagar;
    private Button btn_active;
    private Button btn_controlled;
    private Button btn_vigilance;
    private Button btn_extinct;
    private String distancia;
    private EditText custom_dst;
    private double latitude;
    private double longitude;
    private int min_counter = 1;

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
        bindService(new Intent(ChefeLF.this, Client_Socket.class), mConnection, Context.BIND_AUTO_CREATE);
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


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GPS.BROADCAST_ACTION));
    }

    private void updateUI(Intent intent) {
        latitude = Double.parseDouble(intent.getStringExtra("LAT"));
        longitude = Double.parseDouble(intent.getStringExtra("LONG"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chefe_lf);

        //start service on create
        doBindService();

        CS.setIn_Fire_Line_Update(true);

        gravity0 = 0f;
        gravity1 = 0f;
        gravity2 = 0f;
        currentDegree=0f;
        image = (ImageView)findViewById(R.id.seta);
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        mSensorManager = (SensorManager)this.getSystemService(this.SENSOR_SERVICE);
        acelarometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometro = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, acelarometro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnometro, SensorManager.SENSOR_DELAY_UI);

        lista_distancias_layout = (ListView) findViewById(R.id.lista_dst);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, distancias);
        lista_distancias_layout.setAdapter(arrayAdapter);

        custom_dst=(EditText)findViewById(R.id.dst_custom);
        btn_enviar_dst = (Button)findViewById(R.id.btn_enviar_dst);
        btn_apagar = (Button)findViewById(R.id.btn_apagar);
        btn_active = (Button)findViewById(R.id.btn_active);
        btn_controlled = (Button)findViewById(R.id.btn_controlled);
        btn_vigilance = (Button)findViewById(R.id.btn_vigilance);
        btn_extinct = (Button)findViewById(R.id.btn_extinct);

        btn_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                alertDialog.setTitle("Estado da Linha de Fogo");
                alertDialog.setMessage("Activo?");

                //Setting Positive "Sim" Button
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ChangeFireLineStatusMessage cfls_msg = new ChangeFireLineStatusMessage(CS.getFirefighter_ID(), 0);
                        try {
                            cfls_msg.build_changefirelinestatus_packet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            CS.send_packet(cfls_msg.getChangefirelinestatus_packet());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Setting Negative "NÃO" Button
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();

            }
        });

        btn_controlled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                alertDialog.setTitle("Estado da Linha de Fogo");
                alertDialog.setMessage("Controlada?");

                //Setting Positive "Sim" Button
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ChangeFireLineStatusMessage cfls_msg = new ChangeFireLineStatusMessage(CS.getFirefighter_ID(), 1);
                        try {
                            cfls_msg.build_changefirelinestatus_packet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            CS.send_packet(cfls_msg.getChangefirelinestatus_packet());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Setting Negative "NÃO" Button
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });

        btn_vigilance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                alertDialog.setTitle("Estado da Linha de Fogo");
                alertDialog.setMessage("Em Vigilância?");

                //Setting Positive "Sim" Button
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ChangeFireLineStatusMessage cfls_msg = new ChangeFireLineStatusMessage(CS.getFirefighter_ID(), 2);
                        try {
                            cfls_msg.build_changefirelinestatus_packet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            CS.send_packet(cfls_msg.getChangefirelinestatus_packet());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Setting Negative "NÃO" Button
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });


        btn_extinct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                alertDialog.setTitle("Estado da Linha de Fogo");
                alertDialog.setMessage("Linha de Fogo Extinta?");

                //Setting Positive "Sim" Button
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ChangeFireLineStatusMessage cfls_msg = new ChangeFireLineStatusMessage(CS.getFirefighter_ID(), 3);
                        try {
                            cfls_msg.build_changefirelinestatus_packet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            CS.send_packet(cfls_msg.getChangefirelinestatus_packet());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Setting Negative "NÃO" Button
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });


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
                    alertDialog.setMessage(distancia + " metros");

                    int dist;
                    final double distlat, distlon, finalLat, finalLon;

                    dist= Integer.parseInt(distancia);
                    distlat= dist*Math.cos((currentDegree*Math.PI)/180);
                    distlon= dist*Math.sin((-currentDegree*Math.PI)/180);

                    finalLat = latitude + 180/Math.PI*(distlat/6378137);
                    finalLon = longitude + 180/Math.PI*(distlon/(638137*Math.cos(Math.PI/180*latitude)));

                    // String Longitude = gps. getLongitude();
                    //=Toast.makeText(getApplicationContext(), Double.toString(finalLat), Toast.LENGTH_LONG).show();
                    Log.e("Fire Line Coordinates", String.valueOf(finalLat) + " > " + String.valueOf(finalLon) + "::" + currentDegree + "::" + longitude + "::" + distlon + "::dist=" + dist + "::distancia=" + distancia);

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //string GPS_DATA=getGPS();
                            //envia_msg(GPS_DATA);
                            //envia a mensagem para o centro de controlo

                            if (min_counter <3) {

                                Toast.makeText(getApplicationContext(), "Obrigatório enviar mais " + (3-min_counter) + " coordenadas", Toast.LENGTH_LONG).show();
                                custom_dst.setText("");

                                min_counter++;
                            }

                            FirelineMessage fl_msg = new FirelineMessage(CS.getFirefighter_ID(), String.valueOf(finalLat), String.valueOf(finalLon));
                            try {
                                fl_msg.build_fireline_packet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                CS.send_packet(fl_msg.getFireline_packet());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

        btn_apagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                distancia = custom_dst.getText().toString().trim();

                if (!(distancia.isEmpty())) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChefeLF.this);
                    alertDialog.setTitle("Apagar LF e Enviar nova distância?");
                    alertDialog.setMessage(distancia + " metros");

                    int dist;
                    final double distlat, distlon, finalLat, finalLon;

                    dist = Integer.parseInt(distancia);
                    distlat = dist * Math.cos(currentDegree);
                    distlon = dist * Math.sin(currentDegree);

                    Log.e("Debug:", "Current degree" + currentDegree);

                    finalLat = latitude + 180 / Math.PI * (distlat / 6378137);
                    finalLon = longitude + 180 / Math.PI * (distlon / (638137 * Math.cos(Math.PI / 180 * latitude)));

                    // String Longitude = gps. getLongitude();
                    //=Toast.makeText(getApplicationContext(), Double.toString(finalLat), Toast.LENGTH_LONG).show();
                    Log.e("Fire Line Coordinates", String.valueOf(finalLat) + " > " + String.valueOf(finalLon));

                    //Setting Positive "Sim" Button
                    alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //string GPS_DATA=getGPS();
                            //envia_msg(GPS_DATA);
                            //envia a mensagem para o centro de controlo

                            ReplaceFireLineMessage fl_msg = new ReplaceFireLineMessage(CS.getFirefighter_ID(), String.valueOf(finalLat), String.valueOf(finalLon));
                            try {
                                fl_msg.build_replacefireline_packet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                CS.send_packet(fl_msg.getReplacefireline_packet());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            custom_dst.setText("");
                            min_counter=1;
                            Toast.makeText(getApplicationContext(), "Obrigatório enviar mais " + (3-min_counter) + " coordenadas", Toast.LENGTH_LONG).show();
                            min_counter++;
                        }
                    });

                    // Setting Negative "NÃO" Button
                    alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //envia a mensagem para o centro de controlo
                            custom_dst.setText("");
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Nada a enviar", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        float degree=currentDegree;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                degree = orientation[0]; // orientation contains: azimut, pitch and roll
                degree = degree * (float) 57.3248408;

            }

        }

        RotateAnimation ra = new RotateAnimation(currentDegree,-degree,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
        //tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chefe_l, menu);

        CS.countDownTimer_LF.cancel();

        if(CS.isFireline_update_request()){
            AcceptRequestMessage ar_msg = new AcceptRequestMessage(CS.getFirefighter_ID());
            try {
                ar_msg.build_acceptrequest_packet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                CS.send_packet(ar_msg.getAcceptrequest_packet());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("Request:", "Accepted");
        }

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
                CS.setAfter_login(false);
                CS.setIn_Fire_Line_Update(false);
                doUnbindService();
                /****************************************************************/

                Intent login_Intent = new Intent(ChefeLF.this, Login.class);
                login_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login_Intent);

                return true;

            case R.id.team:

                Intent team_Intent= new Intent(ChefeLF.this, ChangeTeam.class);
                team_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(team_Intent);

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
                CS.setAfter_login(false);
                CS.setIn_Fire_Line_Update(false);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    CS.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doUnbindService();
                /****************************************************************/

                Intent connection_Intent = new Intent(ChefeLF.this, Connection.class);
                connection_Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(connection_Intent);
                return true;


            case R.id.gsm:

                if (CS.getGSM_Status()){
                    CS.setGSM(false);
                    Toast.makeText(getApplicationContext(), "GSM desactivado", Toast.LENGTH_LONG).show();
                }else{
                    CS.setGSM(true);
                    Toast.makeText(getApplicationContext(), "GSM activado", Toast.LENGTH_LONG).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        CS.setIn_Fire_Line_Update(false);
        Intent Intent = new Intent(ChefeLF.this, ChefeMain.class);
        Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent);
    }

}
