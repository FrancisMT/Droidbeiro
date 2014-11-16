package pt.up.fe.droidbeiro.Presentation;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import pt.up.fe.droidbeiro.R;

public class BombeiroMain extends Activity {

    // Initialize the array
    String[] monthsArray = { "JAN", "FEB", "MAR", "APR", "MAY", "JUNE", "JULY", "AUG", "SEPT", "OCT", "NOV", "DEC" };

    // Declare the UI components
    private ListView monthsListView;

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bombeiro_main);

        // Hiding the action bar
        getActionBar().hide();

        monthsListView = (ListView) findViewById(R.id.lista_mensagens);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, monthsArray);
        monthsListView.setAdapter(arrayAdapter);




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
