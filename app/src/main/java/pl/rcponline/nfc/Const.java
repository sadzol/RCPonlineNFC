package pl.rcponline.nfc;

public class Const {

    public static final String ENIVORMENT   = "/app_dev.php";
//    public static final String ENIVORMENT   = "";
//    public static final String MAIN_URL     = "http://dev-panel.rcponline.pl";//
    public static final String MAIN_URL     = "http://panel.rcponline.pl";//
//    public static final String MAIN_URL     = "http://192.168.2.102";//rcp.lh  home
//    public static final String MAIN_URL   = "http://192.168.1.107"; //biuro
//    public static final String MAIN_URL   = "http://rcp.lh"; //mietka
    public static final String LOGIN_URL    = MAIN_URL+ENIVORMENT+"/apiNfc/loginNfc";
    public static final String ADD_EVENT_URL  = MAIN_URL+ENIVORMENT+"/apiNfc/addEvent";
    public static final String ADD_EVENTS_URL = MAIN_URL+ENIVORMENT+"/apiNfc/addEvents";
    public static final String URL_TEST     = MAIN_URL+ENIVORMENT+"/apiNfc/sT";
    public static final String PACKAGE      = "pl.rcponline.nfc";

    public static final String LOGIN_API_KEY        = "login";
    public static final String PASSWORD_API_KEY     = "password";
    public static final String TYPE_ID_API_KEY      = "type_id";
    public static final String SOURCE_ID_API_KEY    = "source_id";
    public static final String DATATIME_API_KEY     = "datetime";
    public static final String LOCATION_API_KEY     = "location";
    public static final String COMMENT_API_KEY      = "comment";
    public static final String EVENTS_API_KEY       = "events";
    public static final String EMPLOYEE_ID_API_KEY  = "employee_id";
    public static final String IDENTIFICATOR_API_KEY= "identificator";
    public static final String DEVICE_CODE_API_KEY  = "device_code";

    public static final Integer SOURCE_ID           = 5;//reader-smartphone
    public static final String PREF_LOGIN           = "pl.rcponline.nfc.login";
    public static final String PREF_PASS            = "pl.rcponline.nfc.password";
    public static final String PREF_IS_USER_LOGGED  = "pl.rcponline.nfc.is_user_logged";
    public static final String PREF_MESSAGE_AFTER_EVENT = "pl.rcponline.nfc.message_after_event";
    public static final String PREF_EMPLOYEE_ID         = "pl.rcponline.nfc.employee_id";
    public static final String PREF_EMPLOYEE_NAME       = "pl.rcponline.nfc.employee_name";
    public static final String PREF_EMPLOYEE_FIRSTNAME  = "pl.rcponline.nfc.employee_firstname";
    public static final String PREF_EMPLOYEE_PERMISSION = "pl.rcponline.nfc.employee_permission";
    public static final String PREF_EMPLOYEE_IDENTIFICATOR = "pl.rcponline.nfc.employee_identificator";
    public static final String PREF_EMPLOYEE_LAST_EVENT_TYPE_ID = "pl.rcponline.nfc.employee_last_event_type_id";
    public static final String PREF_DEVICE_CODE     = "pl.rcponline.nfc.device_code";


    public static final Integer TIME_INTERVAL = 1000 * 60 * 2;

    public static final String[] EVENT_TYPE = {"work_start","break_start","break_finish","temp_out_start","temp_out_finish","work_finish"};


}
