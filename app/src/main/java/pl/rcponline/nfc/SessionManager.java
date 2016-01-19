package pl.rcponline.nfc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Random;

public class SessionManager {

    //private static final String TAG = "SESSION_MANAGER";
//    private static final String PREF_NAME = "RCP_NFC_PREF_SESSION";
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    //SharedPreference Mode
//    private int PREF_MODE = 0;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(Const.PREF_NAME, Const.PREF_MODE);
        editor = pref.edit();
    }

    public void createSession(String login ,String pass){

        editor.putString(Const.PREF_LOGIN, login);
        //TODO haslo trzeba zapisac w jakies zakodowanej formie
        editor.putString(Const.PREF_PASS, pass);
        editor.putBoolean(Const.PREF_IS_USER_LOGGED, true);

        editor.apply();
    }

    public String getLogin(){
        return pref.getString(Const.PREF_LOGIN,"");
    }
    public String getPassword(){
        return pref.getString(Const.PREF_PASS, "");
    }

    public void setEmployeeId(long id){
        editor.putLong(Const.PREF_EMPLOYEE_ID, id);
        editor.apply();
    }
    public Long getEmployeeId(){
        return pref.getLong(Const.PREF_EMPLOYEE_ID, 0);
    }
    public void setEmployeeName(String name){
        editor.putString(Const.PREF_EMPLOYEE_NAME, name);
        editor.apply();
    }
    public String getEmployeeName(){
        return pref.getString(Const.PREF_EMPLOYEE_NAME, "");
    }
    public void setEmployeePermission(int permission){
        editor.putInt(Const.PREF_EMPLOYEE_PERMISSION, permission);
        editor.apply();
    }
    public Integer getEmployeePermission(){
        return pref.getInt(Const.PREF_EMPLOYEE_PERMISSION, 0);
    }

    public void setIdentificator(String number){
        editor.putString(Const.PREF_EMPLOYEE_IDENTIFICATOR, number);
        editor.apply();
    }
    public String getIdentificator(){
        return pref.getString(Const.PREF_EMPLOYEE_IDENTIFICATOR, "");
    }

    public void setLastEventTypeId(int typeId){
        editor.putInt(Const.PREF_EMPLOYEE_LAST_EVENT_TYPE_ID, typeId);
        editor.apply();
    }
    public Integer getLastEventTypeId(){
        return pref.getInt(Const.PREF_EMPLOYEE_LAST_EVENT_TYPE_ID, 6);
    }

    public void setDeviceCode(){
        String deviceCode =  pref.getString(Const.PREF_DEVICE_CODE, "");
        if(deviceCode == ""){
            String randomCode = getRandomString(6);
            editor.putString(Const.PREF_DEVICE_CODE,randomCode);
            editor.apply();
        }

    }
    public String getDeviceCode(){
        String deviceCode =  pref.getString(Const.PREF_DEVICE_CODE, "");
        if(deviceCode == ""){
            setDeviceCode();
        }

        return pref.getString(Const.PREF_DEVICE_CODE, "");
    }

    public void addMessage(String message){
        editor.putString(Const.PREF_MESSAGE_AFTER_EVENT, message);
        editor.apply();
    }

    //sprawdza czy zalogowany jesli nie przenosci do Login Activity
    public boolean checkLogin(){
        if(!isUserLoggedIn()){
            goToLoginActivity();
            return true;
        }
        return false;

    }

    public void logout(){

        editor.putString(Const.PREF_LOGIN, "");
        editor.putString(Const.PREF_PASS, "");
        editor.putBoolean(Const.PREF_IS_USER_LOGGED, false);

        editor.apply();
        //commit jest przestarzale (synchorniczne ) blokuje caly watek dopoki nie zapisze
        //editor.commit();

        goToLoginActivity();

    }

    public void clearEmployee(){
        setEmployeeId(0);
        setEmployeeName("");
        setEmployeePermission(0);
        setLastEventTypeId(0);
    }
    public boolean isUserLoggedIn(){
        return pref.getBoolean(Const.PREF_IS_USER_LOGGED, false);
    }

    private void goToLoginActivity(){
        //Po wylogowaniu przekierowanie na LoginActivity
        Intent intent = new Intent(context, LoginActivity.class);

        //Zamykanie wszystkich innych aktywnosci z aplikacji skoro nastąpiło wylogowanie
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Dodaj nowa Flage na poczatek nowej Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Start Login Activity
        context.startActivity(intent);
    }

    //Zmagazynowane dane
    public HashMap<String, String> getData(){

        HashMap<String, String> data = new HashMap<String, String>();
        data.put(Const.PREF_LOGIN, pref.getString(Const.PREF_LOGIN, null));
        data.put(Const.PREF_PASS, pref.getString(Const.PREF_PASS, null));
        data.put(Const.PREF_MESSAGE_AFTER_EVENT, pref.getString(Const.PREF_MESSAGE_AFTER_EVENT, null));

        return data;
    }



    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }


    public void setIsSynchroNow(boolean isSynchro){
//        Log.d("SYNCHRONIZACJA_MAN_SET",String.valueOf(isSynchro));
        editor.putBoolean(Const.PREF_IS_SYNCHRONISATION_NOW, isSynchro);
        editor.apply();
    }
    public boolean getIsSynchroNow(){
        boolean isSynchro = pref.getBoolean(Const.PREF_IS_SYNCHRONISATION_NOW, false);
//        Log.d("SYNCHRONIZACJA_MAN_GET",String.valueOf(isSynchro));
        return isSynchro;

    }
}
