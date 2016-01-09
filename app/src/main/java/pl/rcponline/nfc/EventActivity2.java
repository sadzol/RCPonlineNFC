package pl.rcponline.nfc;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.rcponline.nfc.adapter.EventsAdapter;
import pl.rcponline.nfc.dao.DAO;
import pl.rcponline.nfc.dao.EventDAO;
import pl.rcponline.nfc.model.Event;

public class EventActivity2 extends Activity implements View.OnClickListener ,ConnectionCallbacks, OnConnectionFailedListener, LocationListener, SurfaceTextureListener {

    private static final String TAG = "EVENT_ACTIVITY";
    AQuery aq;
    String  comment, data, location;
    int isEventSend, typeId,lastEvenTypeId;
    View lasViewEvent;
    EditText etComment;
    LinearLayout llLastEvent;
    RelativeLayout rlPayExit, rlBreak;

    // SESSION MANAGER CLASS
    SessionManager session;
    ProgressDialog pd;
    Context context;

    ImageButton btStart, btFinish, btBreakStart, btBreakFinish, btTempStart, btTempFinish;
    ImageView imSynchro,ivStartOff,ivFinishOff,ivBreakOff,ivTempOff;
    LinearLayout llDatatime;

    /////CAMERA//////
    private TextureView myTexture;
//    private SurfaceHolder surfaceHolder;
//    private SurfaceView svCameraPreview;
    private Camera camera;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    ///////////////LOCATION///////////////////////////////////////////////////////
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 1001;
    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 1 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 1; // 1 meters
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Jesli user NIE zalogowany przenies do strony logowania zamykajac ta aktywnosc
        session = new SessionManager(getApplicationContext());
        if (session.checkLogin()) {
            finish();
        }

        //FULL SCREEN BEFORE setContetnView
        if(session.getEmployeePermission() < 1) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //END FULL SCREEN

        setContentView(R.layout.activity_event2);

        //Ustawiamy ustawienia domyślnymi warościami z pliku prefernecji (false - tylko raz)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        context = this;

        ///////////LOC///////// First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            Log.d(TAG,"checkPlayService - OK");
            buildGoogleApiClient();
            createLocationRequest();
        }else{
            Log.d(TAG,"checkPlayService - NO");
        }
        //////////////////////////////////////////////////

//        Zegar w czasie rzeczywistym
//        Runnable myRunnableThread = new CountDownRunner();
//        Thread myThread = new Thread(myRunnableThread);
//        myThread.start();

        //AQuery
        aq = new AQuery(getApplicationContext());

        setTitle(session.getEmployeeName());

        //Wył. KLAWIATURE do czasu az pole tekstownie nie zostanie wybrane    (Disabled software keyboard in android until TextEdit is chosen)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Inicjajca przyciskow
        btStart         = (ImageButton) findViewById(R.id.bt_start);
        btFinish        = (ImageButton) findViewById(R.id.bt_finish);
        btBreakStart    = (ImageButton) findViewById(R.id.bt_break_start);
        btBreakFinish   = (ImageButton) findViewById(R.id.bt_break_finish);
        btTempStart     = (ImageButton) findViewById(R.id.bt_temp_start);
        btTempFinish    = (ImageButton) findViewById(R.id.bt_temp_finish);

        ivStartOff = (ImageView) findViewById(R.id.iv_start_off);
        ivFinishOff= (ImageView) findViewById(R.id.iv_finish_off);
        ivBreakOff = (ImageView) findViewById(R.id.iv_break_off);
        ivTempOff  = (ImageView) findViewById(R.id.iv_temp_off);
        llDatatime = (LinearLayout)findViewById(R.id.ll_datatime);
        imSynchro = (ImageView) findViewById(R.id.im_synchronized);

        //Potrzebne dla ustawien widocznosci
        etComment = (EditText) findViewById(R.id.et_event_comment);
        llLastEvent =(LinearLayout) findViewById(R.id.ll_last_events);
        rlBreak = (RelativeLayout) findViewById(R.id.ll_pause);
        rlPayExit= (RelativeLayout) findViewById(R.id.ll_record);

        //Podglad kamerki
//        svCameraPreview = (SurfaceView) findViewById(R.id.sv_camera_preview);

//        myTexture = (TextureView) findViewById(R.id.sv_camera_preview);
//        myTexture = new TextureView(this);
//        myTexture.setSurfaceTextureListener(this);
//        setContentView(myTexture);
//        myTexture.setAlpha(1.0f);
//        myTexture.setRotation(90.0f);


        btStart.setOnClickListener(this);
        btFinish.setOnClickListener(this);
        btBreakStart.setOnClickListener(this);
        btBreakFinish.setOnClickListener(this);
        btTempStart.setOnClickListener(this);
        btTempFinish.setOnClickListener(this);
        imSynchro.setOnClickListener(this);

        pd = new ProgressDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.setTitle("");
        pd.setMessage(getString(R.string.searching));

    }

    @Override
    protected void onStart() {

        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        Log.d(TAG, "onStop");
    }

    @Override
    //onResume po odwroceniu urzadzenia
    protected void onResume() {
        super.onResume();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected()){
            //Toast.makeText(this,"StartLocationUpdates",Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }else{
            //Toast.makeText(this,"NOstartLocationUpdates",Toast.LENGTH_SHORT).show();
        }

        if(session.getEmployeeId() == 0 ){
            //powrot na strone akrywacji
            Toast.makeText(context,"Nie zidentyfikowano pracownika",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        //todo ustawic przyciski i odswiezyc eventy
        setButtons();
//        viewLastEvents();

        //TODO sprawdzic czy Pole komentarz ma byc widoczne
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(sp.getBoolean("comment",false)){
            etComment.setVisibility(View.VISIBLE);
        }else {
            etComment.setVisibility(View.GONE);
        }
        if(sp.getBoolean("last_events",false)){
            llLastEvent.setVisibility(View.VISIBLE);
        }else{
            llLastEvent.setVisibility(View.GONE);
        }
        if(sp.getBoolean("break",false)){
            rlBreak.setVisibility(View.VISIBLE);
        }else{
            rlBreak.setVisibility(View.GONE);
        }
        if(sp.getBoolean("pay_exit",false)){
            rlPayExit.setVisibility(View.VISIBLE);
        }else {
            rlPayExit.setVisibility(View.GONE);
        }
//        surfaceHolder = svCameraPreview.getHolder();
//        surfaceHolder.addCallback(this);
//        if (sp.getBoolean("make_foto_preview",true)){
//            svCameraPreview.setVisibility(View.VISIBLE);
//
//        }else{
//            svCameraPreview.setVisibility(View.INVISIBLE);
//        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //If employerPermission > 0 that is manager
        if(session.getEmployeePermission() > 0) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.event_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        //Przechwytuje opcje z górnego menu po prawej
        switch (item.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_employes_list:
                Intent intent1 = new Intent(this,EmployeeActivity.class);
                startActivity(intent1);
                return true;

            case R.id.menu_home:
                Intent intent2 = new Intent(this,MainActivity.class);
                startActivity(intent2);
                return true;

            case R.id.menu_device_code:
                FragmentManager manager = getFragmentManager();
                DeviceCodeFragment dcf = new DeviceCodeFragment();
                dcf.show(manager,"device_code");
                return true;

            case R.id.menu_logout:
                //todo logout moze service
                session.logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        lasViewEvent = v;
//        Log.d(TAG,"cos");
//        Log.d(TAG,String.valueOf(v.getId()));
//        Log.d(TAG,String.valueOf(R.id.im_synchronized));
        if(v.getId() == R.id.im_synchronized){
            Log.d(TAG, "in");
            synchronizedWithServer();
        }else {
            startEvent();
        }


    }
    private void startEvent(){
        View v = lasViewEvent;
        switch (v.getId()) {
            case R.id.bt_start:
                typeId = 1;
                //SendEvent(1);
                break;
            case R.id.bt_finish:
                typeId = 6;
                //SendEvent(6);
                break;
            case R.id.bt_break_start:
                typeId = 2;
                //SendEvent(2);
                break;
            case R.id.bt_break_finish:
                typeId = 3;
                //SendEvent(3);
                break;
            case R.id.bt_temp_start:
                typeId = 4;
                //SendEvent(4);
                break;
            case R.id.bt_temp_finish:
                typeId = 5;
                //SendEvent(5);
                break;
        }
        Log.d(TAG, "EVENTQ " + typeId);

        if(SendEvent()) {
            //TODO info ??? o zapisaniu eventa?
        }
    }

    //To umiecic w EVENT.java-nie moge bo po wykonianu  aq.ajax nie bede mial wplywu na UI, a w mainActivity jest wpylyw na modyfikacje UI
    private boolean SendEvent() {
        dispatchTakePictureIntent();
        //SAVE PICTURE
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("make_foto",true)){
//            camera.takePicture(null,null,picHandler);
        }


        //TODO SPRAWDZIC CZY JEST polaczenie jesli nie to nie uruchamiac aq.ajax
        EditText etComment = (EditText)findViewById(R.id.et_event_comment);
        comment = etComment.getText().toString();
        String url = Const.ADD_EVENT_URL;
        data = getDateTime();
        isEventSend = 0;


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        location = getString(R.string.location_disabled);
        if(mLastLocation != null) {
            location = mLastLocation.getLatitude() + ";" + mLastLocation.getLongitude();
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            Log.d(TAG, "INTERNET-ON");
            //location = gpsTracker.getLatitude()+";"+gpsTracker.getLongitude();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(Const.LOGIN_API_KEY, session.getLogin());
            params.put(Const.PASSWORD_API_KEY, session.getPassword());
            //params.put(Const.LOGIN_API_KEY, "sadzol@tlen.pl");
            //params.put(Const.PASSWORD_API_KEY, "sas");
//            params.put(Const.LOCATION_API_KEY, location);
            params.put(Const.TYPE_ID_API_KEY, typeId);
            params.put(Const.SOURCE_ID_API_KEY, Const.SOURCE_ID);
            params.put(Const.DATATIME_API_KEY, data);
            params.put(Const.LOCATION_API_KEY, location);
            params.put(Const.COMMENT_API_KEY, comment);

            params.put(Const.IDENTIFICATOR_API_KEY, session.getIdentificator());
            params.put(Const.EMPLOYEE_ID_API_KEY, session.getEmployeeId());
            params.put(Const.DEVICE_CODE_API_KEY, session.getDeviceCode());

            Log.d(TAG, "API SEND: login=" + session.getLogin() + ", pass=" + session.getPassword() + ", typeId=" + typeId + ", sourceId=" + Const.SOURCE_ID + ", datatime=" + data + ", id=" + session.getIdentificator() + ", employeeId=" + session.getEmployeeId() + ", device_code=" + session.getDeviceCode());

            ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setInverseBackgroundForced(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setMessage(getString(R.string.please_wait));

//            Log.d(TAG, url);
//            Log.d(TAG, params.toString());
            aq.progress(dialog).ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    String message, error = null;
                    if (json != null) {
                        if (json.optBoolean("success") == true) {
                            Log.i(TAG, "Succes");
                            isEventSend = 1;
                            //message = "suc" + status.getCode() + json.toString() + json.optString("message");

                        } else {
                            Log.i(TAG, "Succes-false");
                            //message = status.getCode() + json.toString() + json.optString("message") + status.getMessage();
                            message = json.optString("message");
//                            error = "BLAD! SerwerRCP: " + message;
                            error = "BLAD! " + message;
                        }
                    } else {
                        Log.i(TAG, "no json");
                        //message = "Error:" + getString(R.string.no_connection) + "( " + status.getCode() + " )";// + json.optString("login");
                        error = "BLAD! Polaczenie: " + status.getMessage();
//                        Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                    }

                    saveEventToLocalDatabase(typeId, data, location, comment, isEventSend, error);

                    //setButtons();
                    //viewLastEvents();
                    goToMainActivityWithToast(error);
//                    finish();
                }
            });

        } else {
            //WYŁ. Internet z karty  DANE MOBILNE OFF
            Log.d(TAG, "INTERNET-OFF");
            saveEventToLocalDatabase(typeId, data, location, comment,isEventSend,"");

            //setButtons();
            //viewLastEvents();
            goToMainActivityWithToast(null);
//            finish();
        }

        return true;
    }

    private void goToMainActivityWithToast(String error){

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        int resourceType = getResources().getIdentifier(String.valueOf(Const.EVENT_TYPE[typeId-1]), "string", getPackageName());
        String msg = context.getString(R.string.event_saved) + " " + context.getString(resourceType).toUpperCase();

        if(error != null){
            msg = error;
        }
        intent.putExtra("event", msg);
//        startActivity(intent);
        //finish();
    }

    private void saveEventToLocalDatabase(int typeId,String data, String location, String comment, int isEventSend, String error){
        EventDAO eventDAO = new EventDAO(context);
        eventDAO.createEvent(typeId, Const.SOURCE_ID, session.getIdentificator(), data, location, comment, isEventSend, session.getEmployeeId(), session.getDeviceCode());
    }

    private void clearSessionEmplyeeData(){
        session.clearEmployee();
    }

    private String getDateTime() {
        SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDataTimeStrong = _format.format(new Date());
        return currentDataTimeStrong;
    }

    /**
     * ZEGAR
     */
    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextView txtCurrentTime = (TextView) findViewById(R.id.tv_time);
                    Date dt = new Date();
                    //int hours = dt.getHours();
                    //int minutes = dt.getMinutes();
                    //int seconds = dt.getSeconds();
                    //String curTime = hours + ":" + minutes + ":" + seconds;
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                    String curTime = df.format(dt.getTime());
                    //String curTime = String.valueOf(dt.getTime());
                    txtCurrentTime.setText(curTime);
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
                    Log.d(TAG, e.toString());
                }
            }
        }
    }

    /**
     * WYSWIETLANIE LISTY OSTATNICH EVENTOW
     */
    public void  viewLastEvents() {

        //Pobieram ostatnie(6) eventy z bazy
        EventDAO eventDAO = new EventDAO(context);
//        Log.d(TAG,"Session EMPLOYEE ID="+String.valueOf(session.getEmployeeId()));
        List<Event> lastEvents = eventDAO.getEventsOfEmployee(session.getEmployeeId());

        //Dodaje adapter do ListView
        ListView lv = (ListView) findViewById(R.id.lv_last_events);
        EventsAdapter adapter = new EventsAdapter(this, lastEvents);
        adapter.notifyDataSetChanged(); //aktualizacja danych
        lv.setAdapter(adapter);

    }

    /**
     * ZMIANA STANU PRZYCISKÓW
     */
    private void setButtons() {
        btStart.setEnabled(false);
        btFinish.setEnabled(false);
        btBreakStart.setEnabled(false);
        btBreakFinish.setEnabled(false);
        btTempStart.setEnabled(false);
        btTempFinish.setEnabled(false);

        btStart.setVisibility(View.INVISIBLE);
        btFinish.setVisibility(View.INVISIBLE);
        btBreakStart.setVisibility(View.INVISIBLE);
        btBreakFinish.setVisibility(View.INVISIBLE);
        btTempStart.setVisibility(View.INVISIBLE);
        btTempFinish.setVisibility(View.INVISIBLE);

        ivStartOff.setVisibility(View.INVISIBLE);
        ivBreakOff.setVisibility(View.INVISIBLE);
        ivFinishOff.setVisibility(View.INVISIBLE);
        ivTempOff.setVisibility(View.INVISIBLE);

        EventDAO eventDAO = new EventDAO(getApplicationContext());
        Event event = eventDAO.getLastEventOfEmployee(session.getEmployeeId());


        if(isCurrentButtonIsOk(event)){
            session.setLastEventTypeId(event.getType());
        }else{
            session.setLastEventTypeId(6);
        }
        Log.d("EVENTID",String.valueOf(session.getLastEventTypeId()));
        switch (session.getLastEventTypeId()) {
            //FINISH
            case 6:
                btStart.setVisibility(View.VISIBLE);
                btStart.setEnabled(true);

                ivBreakOff.setVisibility(View.VISIBLE);
                ivTempOff.setVisibility(View.VISIBLE);
                ivFinishOff.setVisibility(View.VISIBLE);
                llDatatime.setBackgroundResource(R.drawable.gradient_red);
                break;

            //BREAK START
            case 2:
                btBreakFinish.setVisibility(View.VISIBLE);
                btBreakFinish.setEnabled(true);

                ivStartOff.setVisibility(View.VISIBLE);
                ivFinishOff.setVisibility(View.VISIBLE);
                ivTempOff.setVisibility(View.VISIBLE);
                llDatatime.setBackgroundResource(R.drawable.gradient_blue);

                break;

            //TEMP START
            case 4:

                btTempFinish.setVisibility(View.VISIBLE);
                btTempFinish.setEnabled(true);
                ivStartOff.setVisibility(View.VISIBLE);
                ivBreakOff.setVisibility(View.VISIBLE);
                ivFinishOff.setVisibility(View.VISIBLE);
                llDatatime.setBackgroundResource(R.drawable.gradient_orange);
                break;

            //BREAK FINISH
            case 3:

            //TEMP FINISH
            case 5:

            //START
            case 1:
                btTempStart.setVisibility(View.VISIBLE);
                btTempStart.setEnabled(true);
                btBreakStart.setVisibility(View.VISIBLE);
                btBreakStart.setEnabled(true);
                btFinish.setVisibility(View.VISIBLE);
                btFinish.setEnabled(true);

                ivStartOff.setVisibility(View.VISIBLE);
                llDatatime.setBackgroundResource(R.drawable.gradient_green);
                break;
            default:

                //DEFAULT CZYLI EVENT_ID 6 (koniec pracy)
                btStart.setVisibility(View.VISIBLE);
                btStart.setEnabled(true);

                ivBreakOff.setVisibility(View.VISIBLE);
                ivTempOff.setVisibility(View.VISIBLE);
                ivFinishOff.setVisibility(View.VISIBLE);
                llDatatime.setBackgroundResource(R.drawable.gradient_red);

                break;
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

            //jeśli są to wysyłamy eventy ze statusem 0
            Gson g = new Gson();
            Type type = new TypeToken<List<Event>>() {}.getType();
            String eventsString = g.toJson(events, type);
            Log.d(TAG, eventsString);
            HashMap<String, Object> eventsJSONObject = new HashMap<String, Object>();
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
                        if (json.optBoolean("success") == true) {
                            Log.d(TAG, "success: true");
                            DAO.saveAllDataFromServer(json, context);

                            viewLastEvents();
                            setButtons();

                        }else{
                            Log.d(TAG, "success: false");
                            message = json.optString("message");
                        }
                    } else {
                        Log.d(TAG, "success - no json");
                        //TODO co z tymi errorami zrobic???
                        //Kiedy kod 500( Internal Server Error)
                        if (status.getCode() == 500) {
                            message = context.getString(R.string.error_500);

                            //Błąd 404 (Not found)
                        } else if (status.getCode() == 404) {
                            message = context.getString(R.string.error_404);

                            //500 lub 404
                        } else {
                            message = context.getString(R.string.error_unexpected);
                        }
                    }

                    if(message != ""){
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, message);
                }
            });
//            setButtons();
//            viewLastEvents();
        }else{
            Toast.makeText(this, getString(R.string.synchronized_off), Toast.LENGTH_LONG).show();
        }
    }

    /////////LOCATION//////////////////

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FATEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
                Log.d(TAG, "checkPlayServices : ConnectionResult.FALSE: isUserRecoverableError :"+resultCode);
            } else {
                Log.d(TAG,"checkPlayServices: This device is not supported.");
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;

        }else{
            Log.d(TAG, "checkPlayServices: ConnectionResult.TRUE");
            return true;
        }
    }
    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        //mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(session.getEmployeePermission() < 1) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
                //Toast.makeText(this,"Back",Toast.LENGTH_SHORT).show();
                return false;
                //return true;
            } else if (keyCode == KeyEvent.KEYCODE_CALL) {
                //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
                //finish();
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Jesli pracownik zapimni odbic wyjscie...
     * Jesli mamy ustawiona prace w nocy  -> nie zmieniamy wyswietlania osatniego eventa
     * Jesli mamy ustawiona prace dniowa  -> zmianiamy dostepnosc przycisku na PLAY
     */
    private boolean isCurrentButtonIsOk(Event event){

        //Czy jest wg. event pracownika czy event jest pusty(nie ma go)
        Log.d("BLAD",String.valueOf(event.getId()));
        if(event.getId() != 0){

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if(sp.getBoolean("night_work",true)) {//nie wiem czemu odwrotnie

                SimpleDateFormat _formatFrom = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat _formatTo = new SimpleDateFormat("yyyyMMdd");
                String currentDate = _formatTo.format(new Date());

                try {
                    Date eventDate = _formatFrom.parse(event.getDatetime());
                    String eventDateString = _formatTo.format(eventDate);

                    if(eventDateString.compareTo(currentDate) < 0 ){
//                        Log.d("BUTTON_", "event < now");
                        return false;
                    }else{
//                        Log.d("BUTTON_", "event > now");
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return true;
                }
            }else{
//                Log.d("BUTTON_", "NIGHT WORK TRUE- noc");
                //praca nocna eventy zostaja zawsze jak sa
                return true;
            }

        }else{
            Log.d("BLAD","Nie ma eventa id=0");
            return false;
        }

    }

    ///////////////////////CAMERA/////////////////////////////
    //CAMERA PREVIEW ?
    Camera.PictureCallback picHandler = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Calendar currentDate = Calendar.getInstance();
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(currentDate.getTime());
            File pictureFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/"+timeStamp+" "+session.getEmployeeName()+".jpg");

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile.toString());
//                Bitmap bitmap = (Bitmap) data;

                fos.write(data);
                fos.close();
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/")));

                Log.d(TAG,"Zapisano foto..." );
//                Toast.makeText(EventActivity.this, "Zapisano foto", Toast.LENGTH_SHORT).show();
            }catch (FileNotFoundException e){
                Log.e(TAG, "Zdjecie sie nie zapisalo, FILE NO FOUND");
            }catch (IOException e){
                Log.e(TAG, "INNY BLAD");
            }
        }
    };
//
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            camera = Camera.open(1);
//            camera.setDisplayOrientation(90);
//            try{
//                camera.setPreviewDisplay(holder);
//                camera.startPreview();
//            }catch (IOException e){
//                Log.e(TAG, "ERROR PODGLAD KAMERY - SURFACE CREATED : " + e.getMessage());
//            };
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            try{
//                camera.setPreviewDisplay(holder);
//                camera.startPreview();
//            }catch (IOException e){
//                Log.e(TAG, "ERROR PODGLAD KAMERY - SURFACE CHANGED : " + e.getMessage());
//            }
//
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//            camera.stopPreview();
//            camera.release();
//            camera = null;
//        }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        camera = Camera.open();
        Camera.Size previewSize = camera.getParameters().getPreviewSize();

        myTexture.setLayoutParams(new FrameLayout.LayoutParams(
                previewSize.width, previewSize.height, Gravity.CENTER));

        try {
            camera.setPreviewTexture(surface);
        }

        catch (IOException t) {
        }
        camera.startPreview();
        myTexture.setAlpha(1.0f);
        myTexture.setRotation(90.0f);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.d(TAG, "SAVE FILE :)");
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
        }
    }
}
