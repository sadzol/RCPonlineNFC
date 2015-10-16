package pl.rcponline.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.rcponline.nfc.dao.DAO;
import pl.rcponline.nfc.dao.EventDAO;
import pl.rcponline.nfc.dao.IdentificatorDAO;
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Event;
import pl.rcponline.nfc.model.Identificator;
import android.os.Handler;

public class MainActivity extends Activity implements View.OnClickListener{

    NfcAdapter nfcAdapter;
    private static String TAG = "MAIN_ACTIVITY";
    Tag tag;
    Runnable myRunnableThread;
    SessionManager session;
    Context context;
    ImageView imSynchro;
    private Handler mHandler = new Handler();
    OrientationEventListener orientationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //FULL SCREEN BEFORE setContetnView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //END FULL SCREEN

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if(rotation == Surface.ROTATION_180){
            Log.d("ORI","180");
        }else{
            Log.d("ORI","0");
        }
//        Toast.makeText(context,rotation,Toast.LENGTH_LONG).show();
        //Log.d("ORI",String.valueOf(rotation));
        //DISABLE NAVIGATION BAR
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        session = new SessionManager(getApplicationContext());
        //session.logout();
        if(session.checkLogin()){
            finish();
        }

        //ZEGAR
        myRunnableThread = new CountDownRunner();
        Thread myThread = new Thread(myRunnableThread);
        myThread.start();

        //NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            //ok nic sie nie dzieje
            //todo zamienic odwrotnie warunek zeby nie bylo pusto
        }else{
            Toast.makeText(this,R.string.nfc_disabled,Toast.LENGTH_LONG).show();
            session.logout();
            finish();
        }

        Intent intentFrom = getIntent();
        Bundle bundle = intentFrom.getExtras();
        if(bundle != null){
            if(bundle.getString("event","") != "") {
                Toast.makeText(context, bundle.getString("event", ""), Toast.LENGTH_LONG).show();
            }
        }

        imSynchro = (ImageView) findViewById(R.id.im_synchronized_main);
        imSynchro.setOnClickListener(this);

        orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
            public void onOrientationChanged(int orientation) {
                Log.d("ORI",String.valueOf(orientation));
//            if(canShow(orientation)){
//                show();
//            } else if(canDismiss(orientation)){
//                dismiss();
//            }
            }
        };
        //reactOnTag("ds32rgf");
    }


    public void onConfigurationChanged(){
        Log.d("ORI","jj");
    }
    //COS Z UKRYWANEIM
//    private Runnable decor_view_settings = new Runnable()
//    {
//        public void run()
//        {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    };

//    public void onConfigurationChanged(Configuration newConfig) {}

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagId = bin2hex(tag.getId());
        Log.d(TAG, tagId);

        super.onNewIntent(intent);

        reactOnTag(tagId);
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
    }

    private void reactOnTag(String tagId){

        IdentificatorDAO idDAO = new IdentificatorDAO(getApplicationContext());
        Identificator identificator = idDAO.getIdentificatorByNumber(tagId);

        if(identificator != null){
            if(identificator.getEmployee() != null){
                Employee emp = identificator.getEmployee();

                session.setEmployeeId(emp.getId());
                session.setEmployeeName(emp.getFirstname() + " " + emp.getName());
                session.setEmployeePermission(emp.getPermission());
                session.setIdentificator(tagId);

                EventDAO eventDAO = new EventDAO(getApplicationContext());
                Event event = eventDAO.getLastEventOfEmployee(emp.getId());
                if(event != null){
                    session.setLastEventTypeId(event.getType());
                }else{
                    session.setLastEventTypeId(6);
                }

                Intent intentNew = new Intent(getApplicationContext(), EventActivity.class);
                startActivity(intentNew);


            }else {
                Toast.makeText(this,tagId, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {

        Log.d(TAG, String.valueOf(v.getId()));
        if(v.getId() == R.id.im_synchronized_main){
            Log.d(TAG, "synchronized");
            synchronizedWithServer();
        }


    }
    @Override
    protected void onResume() {

        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        //TODO wlaczyc dla REALNEGO URZADZENIA
        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
        }else{
            //Toast.makeText(this,R.string.nfc_disabled,Toast.LENGTH_LONG).show();
        }
        Log.d("ORI", "r");
//        orientationListener.enable();
        super.onResume();
    }

    @Override
    protected void onPause() {


        if(nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

//        orientationListener.disable();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.main_menu:
//                DAO.synchronizedWithServer(getApplicationContext());
                synchronizedWithServer();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ZEGAR
     */
    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextView txtCurrentTime = (TextView) findViewById(R.id.tv_time);
                    TextView txtCurrentDate = (TextView) findViewById(R.id.tv_date);
                    Date dt = new Date();
                    //int hours = dt.getHours();
                    //int minutes = dt.getMinutes();
                    //int seconds = dt.getSeconds();
                    //String curTime = hours + ":" + minutes + ":" + seconds;
                    SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
                    String curTime = dfTime.format(dt.getTime());

                    SimpleDateFormat dfDate = new SimpleDateFormat("dd.MM.yyyy");
                    String curDate = dfDate.format(dt.getTime());
                    //String curTime = String.valueOf(dt.getTime());
                    txtCurrentTime.setText(curTime);
                    txtCurrentDate.setText(curDate);

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        });
    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Log.d(TAG,e.toString());
                }
            }
        }
    }

    private void synchronizedWithServer(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {

            EventDAO eventDAO = new EventDAO(context);
            List<Event> events = eventDAO.getEventsWithStatus(0);

            ProgressDialog dialog = new ProgressDialog(context,ProgressDialog.THEME_HOLO_DARK);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setInverseBackgroundForced(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setMessage(context.getString(R.string.synchronized_with_server));

            //jesli sa to wysylamy eventy ze statusem 0
            Gson g = new Gson();
            Type type = new TypeToken<List<Event>>() {}.getType();
            String eventsString = g.toJson(events, type);
            Log.d(TAG, eventsString);
            HashMap<String, Object> eventsJSONObject = new HashMap<String, Object>();
//            HashMap<String, Object> eventsJSONObject = new HashMap<>();
            SessionManager session = new SessionManager(context);

            eventsJSONObject.put(Const.LOGIN_API_KEY, session.getLogin());
            eventsJSONObject.put(Const.PASSWORD_API_KEY, session.getPassword());
            eventsJSONObject.put(Const.EVENTS_API_KEY, eventsString);
            Log.d(TAG, eventsJSONObject.toString());

            AQuery aq = new AQuery(context);
            String url = Const.ADD_EVENTS_URL;
            aq.progress(dialog).ajax(url, eventsJSONObject, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    String message = "";

                    if (json != null) {
                        //zmienna succes moze byc albo true albo false
                        if (json.optBoolean("success")) {
                            DAO.saveAllDataFromServer(json, context);

                        }
                    } else {
                        //TODO co z tymi errorami zrobic???
                        //Kiedy kod 500( Internal Server Error)
                        if (status.getCode() == 500) {
                            message = context.getString(R.string.error_500);

                            //Blad 404 (Not found)
                        } else if (status.getCode() == 404) {
                            message = context.getString(R.string.error_404);

                            //500 lub 404
                        } else {
                            message = context.getString(R.string.error_unexpected);
                        }
                    }
                    Log.i(TAG, message);
                }
            });
        }else{
            Toast.makeText(this, getString(R.string.synchronized_off), Toast.LENGTH_LONG).show();
        }
    }

    //BLOKUJE PRZCYISK HOME
//    @Override
//    public void onAttachedToWindow() {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//        super.onAttachedToWindow();
//    }

    //BLOKUJE PRZYCISK BACK
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG,"KEY");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            //Toast.makeText(this,"Back",Toast.LENGTH_SHORT).show();
            return false;
            //return true;
        }else if (keyCode == KeyEvent.KEYCODE_CALL) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            //finish();
            Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
            return false;

            //OD UKRYWANIA NAVIGATION BAR
        }else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Toast.makeText(this,"key",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "KEY-OK");
//            mHandler.postDelayed(decor_view_settings, 500);
        }

        return super.onKeyDown(keyCode, event);
    }

}
