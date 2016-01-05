package pl.rcponline.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    ImageView imSynchro,imWifi,imBattery,imGsm;

    WifiReceiver receiverWifi;
    TelephonyManager telephonyManager;
    myPhoneStateListener psListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //FULL SCREEN BEFORE setContetnView
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //END FULL SCREEN

////        //GSM
//        imGsm     = (ImageView) findViewById(R.id.im_gsm);
//        psListener = new myPhoneStateListener();
//        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        //Ustawienie layout w zaleznosci od pionowego polozenia
//        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int rotation = display.getRotation();
//        if(rotation == Surface.ROTATION_180){
//            setContentView(R.layout.activity_main);
//        }else{
//            setContentView(R.layout.activity_main_revers);
//        }
//        //END

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

////        //GSM
//        imGsm     = (ImageView) findViewById(R.id.im_gsm);
//        psListener = new myPhoneStateListener();
//        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

//        orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
//            public void onOrientationChanged(int orientation) {
//                Log.d("1ORI",String.valueOf(orientation));
////            if(canShow(orientation)){
////                show();
////            } else if(canDismiss(orientation)){
////                dismiss();
////            }
//            }
//        };
        //reactOnTag("ds32rgf");
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

//                EventDAO eventDAO = new EventDAO(getApplicationContext());
//                Event event = eventDAO.getLastEventOfEmployee(emp.getId());
//                if(event != null){
//                    session.setLastEventTypeId(event.getType());
//                }else{
//                    session.setLastEventTypeId(6);
//                }

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

        //Reaguje na zmiane ustawienia pionowego czytnika
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if(rotation == Surface.ROTATION_180){
            setContentView(R.layout.activity_main);
        }else{
            setContentView(R.layout.activity_main_revers);
        }
        imSynchro = (ImageView) findViewById(R.id.im_synchronized_main);
        imSynchro.setOnClickListener(this);

        //WIFI STRONG SIGNAL
        imWifi = (ImageView) findViewById(R.id.im_wifi);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        //Battery
        imBattery = (ImageView) findViewById(R.id.im_battery);
        registerReceiver(battery_receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



        //END
        super.onResume();

        //        //GSM
        imGsm     = (ImageView) findViewById(R.id.im_gsm);
        psListener = new myPhoneStateListener();
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        //GSM SIGNAL
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        unregisterReceiver(battery_receiver);

        if(nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
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
                    //String curTime = hours + ":" + minutes + ":" + seconds;


                    //PROBLEM Z MIGAJACYM :  jest taki ze pusty znak i : maja rozne szerokosci
//                    SimpleDateFormat dfHours = new SimpleDateFormat("HH");
//                    SimpleDateFormat dfMinutes = new SimpleDateFormat("mm");
//                    SimpleDateFormat dfSeconds = new SimpleDateFormat("ss");
//
//                    String seconds = dfSeconds.format(dt.getTime());
//                    String minutes = dfMinutes.format(dt.getTime());
//                    String hours   = dfHours.format(dt.getTime());
//
//                    Log.d("CLOCK",seconds);
//
//                    String curTime = "";
//                    if((Integer.valueOf(seconds) % 2) == 0){
//                        curTime = hours + "_" + minutes;
//                    }else{
//                        curTime = hours + ":" + minutes;
//                    }


                    SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
                    String curTime = dfTime.format(dt.getTime());

                    SimpleDateFormat dfDate = new SimpleDateFormat("dd.MM.yyyy");
                    String curDate = dfDate.format(dt.getTime());
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

    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int state = wifi.getWifiState();
            if (state == WifiManager.WIFI_STATE_ENABLED) {
//                Log.d("WIFI", "wifi=on");
//                tvWifiStatus.setText("WIFI = ON");
                List<ScanResult> results = wifi.getScanResults();

                for (ScanResult result : results) {
                    if (result.BSSID.equals(wifi.getConnectionInfo().getBSSID())) {
                        int level = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(), result.level);
                        int difference = level * 100 / result.level;
                        int signalStrangth = 0;
                        if (difference >= 100) {
                            signalStrangth = 4;
//                            imWifi.setImageDrawable(R.drawable.wifi_level_4);//ImageResource(R.id.wifi_level_4);
                        } else if (difference >= 75){
                            signalStrangth = 3;
//                            imWifi.setImageResource(R.id.wifi_level_3);
                        } else if (difference >= 50){
                            signalStrangth = 2;
//                            imWifi.setImageResource(R.id.wifi_level_2);
                        }else if (difference >= 25){
                            signalStrangth = 1;
//                            imWifi.setImageResource(R.id.wifi_level_1);
                        }
                        int resourceType = getResources().getIdentifier("wifi_level_" + signalStrangth, "drawable", getPackageName());
                        imWifi.setImageResource(resourceType);
                        Log.d("WIFI", "Difference :" + difference + " signal state:" + signalStrangth);
                    }
                }
            }else{
//                Log.d("WIFI", "wifi=on");
                int resourceType = getResources().getIdentifier("wifi_level_0", "drawable", getPackageName());
                imWifi.setImageResource(resourceType);
            }

        }
    }
    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            int scale = intent.getIntExtra("scale", -1);
            int status = intent.getIntExtra("status", 0);
            int rawlevel = intent.getIntExtra("level", -1);
            int level = 0;
            int resourceType;

            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                int levelFour;
                if (level >= 100) {
                    levelFour = 4;
//                            imWifi.setImageDrawable(R.drawable.wifi_level_4);//ImageResource(R.id.wifi_level_4);
                } else if (level >= 75){
                    levelFour = 3;
//                            imWifi.setImageResource(R.id.wifi_level_3);
                } else if (level >= 50){
                    levelFour = 2;
//                            imWifi.setImageResource(R.id.wifi_level_2);
                }else if (level >= 25){
                    levelFour = 1;
//                            imWifi.setImageResource(R.id.wifi_level_1);
                }else{
                    levelFour = 0;
                }
//                Log.d("WIFI","raw:"+levelFour);
//                String info = "Battery: " + level + "%\n";
//                info += ("Battery Status: " + getStatusString(status) + "\n");
//                textBatteryLevel.setText(info);
//                + "\n\n" + bundle.toString());
                if(getStatusString(status).equals("Discharging")){
                    resourceType = getResources().getIdentifier("battery_level__0", "drawable", getPackageName());
                    Log.d("WIFI","rozlad");
                }else{
                    Log.d("WIFI",""+levelFour);
                    resourceType = getResources().getIdentifier("battery_level_"+levelFour, "drawable", getPackageName());
                }

            } else {
//                textBatteryLevel.setText("Battery not present!!!");
                resourceType = getResources().getIdentifier("battery_level_0", "drawable", getPackageName());
            }
            imBattery.setImageResource(resourceType);
        }
    };


    private String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }

        return statusString;
    }

    //GSM
    public class myPhoneStateListener extends PhoneStateListener {
        private int signalStrengthValue;
        private  int level;
        private  int GSM_SIGNAL_STRENGTH_GREAT = 12;
        private  int GSM_SIGNAL_STRENGTH_GOOD = 8;
        private  int GSM_SIGNAL_STRENGTH_MODERATE = 5;

        private  int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
        private  int SIGNAL_STRENGTH_POOR = 1;
        private  int SIGNAL_STRENGTH_MODERATE = 2;
        private  int SIGNAL_STRENGTH_GOOD = 3;
        private  int SIGNAL_STRENGTH_GREAT = 4;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
                // asu = 0 (-113dB or less) is very weak
                // signal, its better to show 0 bars to the user in such cases.
                // asu = 99 is a special case, where the signal strength is unknown.
                int asu = signalStrength.getGsmSignalStrength();
                if (asu <= 2 || asu == 99) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                else if (asu >= GSM_SIGNAL_STRENGTH_GREAT) level = SIGNAL_STRENGTH_GREAT;
                else if (asu >= GSM_SIGNAL_STRENGTH_GOOD)  level = SIGNAL_STRENGTH_GOOD;
                else if (asu >= GSM_SIGNAL_STRENGTH_MODERATE)  level = SIGNAL_STRENGTH_MODERATE;
                else level = SIGNAL_STRENGTH_POOR;

                signalStrengthValue= level;
                Log.d("GSM","GSM Signal: " + signalStrengthValue);
            } else {
                Log.d("GSM","GSM Signal: " + signalStrengthValue);
                signalStrengthValue = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;// signalStrength.getCdmaDbm();
            }
            int resourceType = getResources().getIdentifier("gsm_level_"+signalStrengthValue , "drawable", getPackageName());
            imGsm.setImageResource(resourceType);
//            Log.d("GSM","GSM Signal: " + signalStrengthValue);
        }
    }
}
