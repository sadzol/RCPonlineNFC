package pl.rcponline.nfc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pl.rcponline.nfc.dao.DAO;
import pl.rcponline.nfc.dao.EmployeeDAO;
import pl.rcponline.nfc.dao.EventDAO;
import pl.rcponline.nfc.dao.IdentificatorDAO;
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Event;
import pl.rcponline.nfc.model.Identificator;

public class LoginActivity extends Activity {

    EditText etLogin,etPassword;
    private final String TAG = "LOGIN_ACTIVITY";
    AQuery aq;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        aq = new AQuery(this);
        etLogin     = (EditText) findViewById(R.id.et_log);
        etPassword  = (EditText) findViewById(R.id.et_password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void signUp(View view) {
        Log.d(TAG, "In");
        final String login = etLogin.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        String url = Const.LOGIN_URL;

        Map<String,Object> params = new HashMap<String,Object>();
        params.put(Const.LOGIN_API_KEY,login);
        params.put(Const.PASSWORD_API_KEY, password);

        ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setMessage(getString(R.string.login));

        aq.progress(dialog).ajax(url,params, JSONObject.class,new AjaxCallback<JSONObject>(){

            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                    Log.d(TAG, String.valueOf(json));
                    if(json != null){
                        //String res;
                        if(json.optBoolean("success") == true){
                            Log.d(TAG, "Success Login: "+login+":"+password);

                            session.createSession(login,password);
                            session.setDeviceCode();
                            /*SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString(Const.PREF_LOGIN, login );
                            editor.putString(Const.PREF_PASS, password);
                            //editor.putString(Const.IS_REQUIRED_LOCATION);
                            editor.commit();
                            setResult(RESULT_OK);*/

//                            String strJson="
//                            {
//                                \"Employee\" :[
//                                {
//                                    \"id\":\"01\",
//                                    \"name\":\"Gopal Varma\",
//                                    \"salary\":\"500000\"
//                                },
//                                {
//                                    \"id\":\"02\",
//                                    \"name\":\"Sairamkrishna\",
//                                    \"salary\":\"500000\"
//                                },
//                                {
//                                    \"id\":\"03\",
//                                    \"name\":\"Sathish kallakuri\",
//                                    \"salary\":\"600000\"
//                                }
//                                ]
//                            }";
//                            String j = "{
//                                \"data\": {
//                                    \"0\": {
//                                        \"id\": 19,
//                                                \"name\": \"ADMIN\",
//                                                \"first_name\": \"SUPER\",
//                                                \"permission\": 1,
//                                                \"identificator\": {
//                                            \"0\": \"21750C1A\"
//                                        },
//                                        \"events\": {
//                                            \"0\": \"{\"type\":5,\"source\":2,\"datetime\":\"2015-07-06 18:51:46\",\"location\":\"127.0.0.1\",\"comment\":\"\",\"status\":1}\",
//                                            \"1\": \"{\"type\":4,\"source\":2,\"datetime\":\"2015-07-06 18:51:45\",\"location\":\"127.0.0.1\",\"comment\":\"\",\"status\":1}\"
//                                        }
//                                     }
//                                },
//                                \"message\": \"Pobrano dane.\",
//                                \"success\": true
//                            }";
//                            DBHelper dbHelper = new DBHelper();

                            DAO.saveAllDataFromServer(json,getApplicationContext());

                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();

                        }else {
                            Log.d(TAG, "fail");
                            Toast.makeText(getApplicationContext(), getString(R.string.bad_login_or_pass), Toast.LENGTH_LONG).show();
                        }

                        //Toast.makeText(getApplicationContext(), status.getCode() + ":" + json.optString("user"), Toast.LENGTH_LONG).show();
                    }else{

                        String error;
                        //Kiedy kod 500( Internal Server Error)
                        if (status.getCode() == 500) {
                            error = getString(R.string.error_500);

                            //Błąd 404 (Not found)
                        } else if (status.getCode() == 404) {
                            error = getString(R.string.error_404);

                            //Blad -101 Moze oznaczac: Serwer nie odpowiada
                        } else if(status.getCode() == -101 ){
                            error = getString(R.string.error_offline);

                            //500 lub 404
                        }else{
                            error = getString(R.string.error_unexpected);
                        }
                        Log.d(TAG, error);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }
            }
        });
    }
}
