package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import pt.up.fe.droidbeiro.R;

public class BombeiroMain extends Activity {

    String[] mensagens =   {    "Need Support",
                                "Need to Back Down",
                                "Firetruck is in Trouble",
                                "Need Aerial Support",
                                "The Fire is Spreading",
                                "We’re Leaving",
                                "Fire Getting Close to House",
                                "House Burned"
                            };

    private ListView mensagens_lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bombeiro_main);

        // Hiding the action bar
        getActionBar().hide();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bombeiro_main, menu);
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
