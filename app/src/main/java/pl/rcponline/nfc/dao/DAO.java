package pl.rcponline.nfc.dao;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import pl.rcponline.nfc.Const;
import pl.rcponline.nfc.R;
import pl.rcponline.nfc.SessionManager;
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Event;
import pl.rcponline.nfc.model.Identificator;


public class DAO {

    private static final String TAG = "DAO";

    public static void synchronizedWithServer(final Context context) {

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

    }

    public static void saveAllDataFromServer(JSONObject json, final Context context) {
        Gson gson = new Gson();
        EmployeeDAO empDAO = new EmployeeDAO(context);
        IdentificatorDAO idDAO = new IdentificatorDAO(context);
        EventDAO eventDAO = new EventDAO(context);

        //czyszczenie tabel
        empDAO.deleteTable();
        idDAO.deleteTable();
        eventDAO.deleteTable();

        long employeeId;
        String name, firstName;
        int permission;

        try {
            JsonParser parser = new JsonParser();
            if (parser.parse(json.getString("data")).isJsonArray()) {

                //!WEZ JsonArray (od GSON)  nie JSONArray (defaut)
                JsonArray jsonArray = parser.parse(json.getString("data")).getAsJsonArray();

                //PONIZEJ 2 metody pobierania JSON z sieci i zapisywanie do bazy(1- json -> pola -> sqlLite, 2- json -> object ->sqlLite)
                for (int i = 0; i < jsonArray.size(); i++) {

                    Log.d(TAG, jsonArray.get(i).toString());
                    JsonObject jsonEmp = jsonArray.get(i).getAsJsonObject();

                    employeeId = jsonEmp.get("id").getAsLong();
                    name = jsonEmp.get("name").getAsString();
                    firstName = jsonEmp.get("first_name").getAsString();
                    permission = jsonEmp.get("permission").getAsInt();

                    Employee emp = empDAO.createEmployee(employeeId, name, firstName, permission);

                    JsonArray jsonArrayIdntificator = jsonEmp.get("identificator").getAsJsonArray();
                    Log.d(TAG, jsonEmp.get("identificator").toString());
                    for (int j = 0; j < jsonArrayIdntificator.size(); j++) {
//                      Identificator ident = gson.fromJson(jsonArrayIdntificator.get(j).getAsString(), Identificator.class);
                        Identificator ident = new Identificator();
                        JsonObject jsonIdentificator = jsonArrayIdntificator.get(j).getAsJsonObject();
                        ident.setNumber(jsonIdentificator.get("number").getAsString());
                        ident.setDesc(jsonIdentificator.get("desc").getAsString());
//                      ident.setNumber(jsonArrayIdntificator.get(j).getAsString());

                        if (ident.getNumber() != null) {
                            ident.setEmployee(emp);
                            idDAO.insertIdentificator(ident);
                        }
                    }
                    JsonArray jsonArrayEvent = jsonEmp.get("events").getAsJsonArray();
                    for (int k = 0; k < jsonArrayEvent.size(); k++) {
                        Event event = gson.fromJson(jsonArrayEvent.get(k).getAsString(), Event.class);

                        if (event != null) {
                            event.setEmployee(emp);
                            eventDAO.insertEvent(event);
                        }
                    }
                }
//                  Log.d(TAG,json.getString("data"));


            } else {
                Log.d(TAG, "no array");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Blad w przetwarzaniu JSON");
        }
    }
}
