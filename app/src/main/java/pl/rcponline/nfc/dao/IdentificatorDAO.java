package pl.rcponline.nfc.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pl.rcponline.nfc.DBHelper;
import pl.rcponline.nfc.dao.EmployeeDAO;
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Identificator;

public class IdentificatorDAO {

    private static final String TAG = "IdentificatorDAO";

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Context context;
    private String[] allColumn = {DBHelper.COLUMN_IDENTIFICATOR_ID,
            DBHelper.COLUMN_IDENTIFICATOR_NUMBER,
            DBHelper.COLUMN_IDENTIFICATOR_DESC,
            DBHelper.COLUMN_IDENTIFICATOR_EMPLOYEE_ID
    };

    public IdentificatorDAO(Context context){
        this.context = context;
        dbHelper = new DBHelper(context);

        //open db
        try{
            open();
        }catch (SQLException e){
            Log.e(TAG,  "SQLExtension on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void open() throws SQLException{
        db = dbHelper.getWritableDatabase();
    }
    public void close(){
        dbHelper.close();
    }

    public Identificator createIdentificator(String number, String desc, long employeeId){
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_IDENTIFICATOR_NUMBER, number);
        values.put(DBHelper.COLUMN_IDENTIFICATOR_DESC, desc);
        values.put(DBHelper.COLUMN_IDENTIFICATOR_EMPLOYEE_ID, employeeId);

        long insertId = db.insert(DBHelper.TABLE_IDENTIFICATOR, null, values);
        Cursor cursor = db.query(DBHelper.TABLE_IDENTIFICATOR, allColumn, DBHelper.COLUMN_IDENTIFICATOR_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        Identificator newIdentificator = cursorToIdentificator(cursor);
        cursor.close();//WAZNE

        return  newIdentificator;
    }

    public long insertIdentificator(Identificator identificator){
        Log.i(TAG, "Add Identificator: " +
                        "number: " + identificator.getNumber() +
                        "desc: " + identificator.getDesc() +
                        ", employeeId: " + identificator.getEmployee().getId()
        );

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_IDENTIFICATOR_NUMBER, identificator.getNumber());
        values.put(DBHelper.COLUMN_IDENTIFICATOR_DESC, identificator.getDesc());
        values.put(DBHelper.COLUMN_IDENTIFICATOR_EMPLOYEE_ID, identificator.getEmployee().getId());

        long result = db.insert(DBHelper.TABLE_IDENTIFICATOR, null, values);

        return result;
    }
    public void deleteIdentificator(long id){

        db.delete(DBHelper.TABLE_IDENTIFICATOR, DBHelper.COLUMN_IDENTIFICATOR_ID + " = " + id, null);
    }

    public List<Identificator> getAllIdentficator(){

        List<Identificator> listIdentificator = new ArrayList<Identificator>();
        Cursor cursor = db.query(DBHelper.TABLE_IDENTIFICATOR, allColumn, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Identificator identificator = cursorToIdentificator(cursor);
                listIdentificator.add(identificator);
                cursor.moveToNext();
            }

        cursor.close();
        return  listIdentificator;
    }

    public List<Identificator> getIdentificatorByEmployee(long employeeId){

        List<Identificator> listIdentificator = new ArrayList<Identificator>();

        Cursor cursor = db.query(
                DBHelper.TABLE_IDENTIFICATOR,
                allColumn,
                DBHelper.COLUMN_IDENTIFICATOR_ID + " = ? ",
                new String[]{ String.valueOf(employeeId)},
                null,null,null
                );

            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Identificator identificator = cursorToIdentificator(cursor);
                listIdentificator.add(identificator);
                cursor.moveToNext();
            }
            cursor.close();
        return listIdentificator;
    }

    public Identificator getIdentificatorByNumber(String number){

        Identificator identificator = new Identificator();

        Cursor cursor = db.query(
                DBHelper.TABLE_IDENTIFICATOR,
                allColumn,
                DBHelper.COLUMN_IDENTIFICATOR_NUMBER + " = ? ",
                new String[]{String.valueOf(number)},
                null, null, null
        );

//        if(cursor != null){
            if(cursor.moveToFirst()) {
                identificator = cursorToIdentificator(cursor);
            }
//        }

        cursor.close();
        return identificator;
    }

    protected Identificator cursorToIdentificator(Cursor cursor){

        Identificator identificator = new Identificator();

        identificator.setId(cursor.getLong(0));
        identificator.setNumber(cursor.getString(1));
        identificator.setDesc(cursor.getString(2));

        //get Employee
        long employeeId = cursor.getLong(3);
        EmployeeDAO dao = new EmployeeDAO(context);
        Employee employee = dao.getEmployeeById(employeeId);
        if(employee != null)
            identificator.setEmployee(employee);

        return identificator;
    }

    public void deleteTable(){
        db.delete(DBHelper.TABLE_IDENTIFICATOR, null, null);
        Log.i(TAG,"Wyczyszono tabele Identificator");
    }

}
