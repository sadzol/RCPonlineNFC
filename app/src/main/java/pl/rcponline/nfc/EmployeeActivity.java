package pl.rcponline.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import pl.rcponline.nfc.adapter.IdentificatorsAdapter;
import pl.rcponline.nfc.dao.IdentificatorDAO;
import pl.rcponline.nfc.model.Identificator;

public class EmployeeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        IdentificatorDAO identificatorDAO = new IdentificatorDAO(this);
        List<Identificator> listIds = identificatorDAO.getAllIdentficator();
        IdentificatorsAdapter identificatorsAdapter = new IdentificatorsAdapter(this,listIds);
        identificatorsAdapter.notifyDataSetChanged();

        ListView listView = (ListView) findViewById(R.id.lv_employes);
        listView.setAdapter(identificatorsAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_employee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
