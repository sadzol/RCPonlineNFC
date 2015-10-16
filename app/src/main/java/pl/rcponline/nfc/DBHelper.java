package pl.rcponline.nfc;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "DBHelper";
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "RcpNfc.db";

    //column of the employee table
    public static final String TABLE_EMPLOYEE                = "employee";
    public static final String COLUMN_EMPLOYEE_ID            = "_id";
    public static final String COLUMN_EMPLOYEE_FIRSTNAME     = "firstname";
    public static final String COLUMN_EMPLOYEE_NAME          = "name";
    public static final String COLUMN_EMPLOYEE_PERMISSION    = "permission";

    //column of the identificator table
    public static final String TABLE_IDENTIFICATOR            = "identificator";
    public static final String COLUMN_IDENTIFICATOR_ID        = "_id";
    public static final String COLUMN_IDENTIFICATOR_NUMBER    = "number";
    public static final String COLUMN_IDENTIFICATOR_DESC      = "desc";
    public static final String COLUMN_IDENTIFICATOR_EMPLOYEE_ID = "employee_id";

    //column of the event table
    public static final String TABLE_EVENT                      = "event";
    public static final String COLUMN_EVENT_ID                  = "_id";
    public static final String COLUMN_EVENT_TYPE_ID             = "type_id";
    public static final String COLUMN_EVENT_SOURCE_ID           = "source_id";
    public static final String COLUMN_EVENT_IDENTIFICATOR       = "identificator";
    public static final String COLUMN_EVENT_DATETIME            = "datetime";
    public static final String COLUMN_EVENT_LOCATION            = "location";
    public static final String COLUMN_EVENT_COMMENT             = "comment";
    public static final String COLUMN_EVENT_STATUS              = "status";
    public static final String COLUMN_EVENT_EMPLOYEE_ID         = "employee_id";
    public static final String COLUMN_EVENT_DEVICE_CODE         = "device_code";

    //SQL statement of the employees table creation
    private static final String SQL_CREATE_TABLE_EMPLOYEES  = "CREATE TABLE IF NOT EXISTS " + TABLE_EMPLOYEE + "("+
//            COLUMN_EMPLOYEE_ID          + " INTEGER PRIMARY KEY, " +
            COLUMN_EMPLOYEE_ID          + " INTEGER, " +
            COLUMN_EMPLOYEE_FIRSTNAME   + " TEXT," +
            COLUMN_EMPLOYEE_NAME        + " TEXT," +
            COLUMN_EMPLOYEE_PERMISSION  + " INTEGER " +
            ");";

    private static final String SQL_CREATE_TABLE_IDENTIFICATORS = "CREATE TABLE IF NOT EXISTS " + TABLE_IDENTIFICATOR + "("+
            COLUMN_IDENTIFICATOR_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_IDENTIFICATOR_NUMBER     + " TEXT," +
            COLUMN_IDENTIFICATOR_DESC       + " TEXT," +
            COLUMN_IDENTIFICATOR_EMPLOYEE_ID + " INTEGER NOT NULL" +
            ");";

    private static final String SQL_CREATE_TABLE_EVENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENT + "("+
            COLUMN_EVENT_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_EVENT_TYPE_ID        + " INTEGER," +
            COLUMN_EVENT_SOURCE_ID      + " INTEGER," +
            COLUMN_EVENT_IDENTIFICATOR  + " TEXT," +
            COLUMN_EVENT_DATETIME       + " DATETIME," +
            COLUMN_EVENT_LOCATION       + " TEXT," +
            COLUMN_EVENT_COMMENT        + " TEXT," +
            COLUMN_EVENT_STATUS         + " INTEGER DEFAULT 0," +
            COLUMN_EVENT_EMPLOYEE_ID    + " INTEGER NOT NULL, " +
            COLUMN_EVENT_DEVICE_CODE    + " TEXT " +
            ");";

    public DBHelper(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_EMPLOYEES);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_IDENTIFICATORS);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.w(TAG, "Upgreading the database form versio " + i + " to " + i1 + ". All data will by deleted.");

        //clear all data
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IDENTIFICATOR);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);

        onCreate(sqLiteDatabase);
    }

}
