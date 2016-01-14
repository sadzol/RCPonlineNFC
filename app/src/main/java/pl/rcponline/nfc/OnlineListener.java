package pl.rcponline.nfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import pl.rcponline.nfc.dao.DAO;
import pl.rcponline.nfc.dao.EventDAO;
import pl.rcponline.nfc.model.Event;

public class OnlineListener extends BroadcastReceiver {

    private final static String TAG = "ONLINE_Listener";
    private static boolean firstConnect = true;
    // SESSION MANAGER CLASS
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private void setIsSynchroNow(boolean isSynchro){
        Log.d("SYNCHRO",String.valueOf(isSynchro));
        editor.putBoolean(Const.PREF_IS_SYNCHRONISATION_NOW, isSynchro);
        editor.apply();
    }
    private boolean getIsSynchroNow(){
        return pref.getBoolean(Const.PREF_IS_SYNCHRONISATION_NOW, false);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        //todo Czy wywoła sie ta intentcja także po wejsciu w strefę online wifi ?  (zarazem musiałoby być i wyjściu z zasiegu sieci)

        if (!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) &&
                !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) &&
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }

        ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (cm == null) {
            return;
        }

        if(pref == null){
            pref = context.getSharedPreferences(Const.PREF_NAME, Const.PREF_MODE);
            editor = pref.edit();
        }

        // Sprawdza czy jesteśmy online
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            //TODO powinno sie tez pojawic kiedy wszedl w siec wi-fi
            Log.d(TAG, "ONLINE");

            //czy w tym samym czasie sie juz nie synchronizuje
            if(!getIsSynchroNow()){
                setIsSynchroNow(true);

                if (firstConnect) {

                    Log.d(TAG, "First connection");
                    EventDAO eventDAO = new EventDAO(context);
                    List<Event> events = eventDAO.getEventsWithStatus(0);

                    //jeśli nie ma niewysłanych eventów to konczymy
                    if (events.isEmpty()) {
                        Log.d(TAG, "NIE MA EVENTOW DO WYSLANIA");
                        return;
                    } else {
                        Log.d(TAG, String.valueOf(events.size()));
                    }

                    //jeśli są to wysyłamy eventy ze statusem 0
                    Gson g = new Gson();
                    Type type = new TypeToken<List<Event>>() {
                    }.getType();
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
                    aq.ajax(url, eventsJSONObject, JSONObject.class, new AjaxCallback<JSONObject>() {
                        @Override
                        public void callback(String url, JSONObject json, AjaxStatus status) {
                            String message = "";

                            if (json != null) {
                                if (json.optBoolean("success") == true) {
                                    Log.d(TAG, "success=true");
                                    DAO.saveAllDataFromServer(json, context);
                                } else {
                                    Log.d(TAG, "success=false");
                                    message = json.optString("message");
                                }

                            } else {
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
                            if (message != "") {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }
                            Log.i(TAG, message);

                            setIsSynchroNow(false);
                        }
                    });
                    firstConnect = false;
                }else{
                    Log.d(TAG,"Synchronizuje sie juz w tym czasie");
                }

            }else{
                firstConnect = true;
//                Log.d(TAG, "Next connection");
            }
        } else {
            firstConnect = true;
            Log.d(TAG, "OFFLINE");
            ///Toast.makeText(context, "OFFLINE",Toast.LENGTH_SHORT).show();
        }

    }
}
