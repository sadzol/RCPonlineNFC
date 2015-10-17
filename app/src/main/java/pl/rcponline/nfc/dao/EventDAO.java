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
import pl.rcponline.nfc.model.Event;

public class EventDAO {

    private static String TAG = "EventDAO";

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Context context;
    private String[] allColumn = { DBHelper.COLUMN_EVENT_ID,
            DBHelper.COLUMN_EVENT_TYPE_ID, DBHelper.COLUMN_EVENT_SOURCE_ID, DBHelper.COLUMN_EVENT_IDENTIFICATOR,
            DBHelper.COLUMN_EVENT_DATETIME, DBHelper.COLUMN_EVENT_LOCATION, DBHelper.COLUMN_EVENT_COMMENT,
            DBHelper.COLUMN_EVENT_STATUS, DBHelper.COLUMN_EVENT_EMPLOYEE_ID, DBHelper.COLUMN_EVENT_DEVICE_CODE};

    public EventDAO(Context context){
        this.context = context;
        dbHelper = new DBHelper(context);
        try{
            open();
        }catch (SQLException e){
            Log.e(TAG,  "SQLExtension on openning database " + e.getMessage());
            e.printStackTrace();
        }

    }
    private void open() throws SQLException{
        db = dbHelper.getWritableDatabase();
    }
    private void close(){
        dbHelper.close();
    }

    public Event createEvent(int typeId, int sourceId, String identificator, String datetime, String location, String comment, int status, long employeeId, String deviceCode){

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_EVENT_TYPE_ID, typeId);
        values.put(DBHelper.COLUMN_EVENT_SOURCE_ID, sourceId);
        values.put(DBHelper.COLUMN_EVENT_IDENTIFICATOR, identificator);
        values.put(DBHelper.COLUMN_EVENT_DATETIME, datetime);
        values.put(DBHelper.COLUMN_EVENT_LOCATION, location);
        values.put(DBHelper.COLUMN_EVENT_COMMENT, comment);
        values.put(DBHelper.COLUMN_EVENT_STATUS, status);
        values.put(DBHelper.COLUMN_EVENT_EMPLOYEE_ID, employeeId);
        values.put(DBHelper.COLUMN_EVENT_DEVICE_CODE, deviceCode);

        long insertId = db.insert(DBHelper.TABLE_EVENT, null, values);
        Cursor cursor = db.query(DBHelper.TABLE_EVENT,allColumn,null,null,null,null,null);

        cursor.moveToFirst();
        Event newEvent = cursorToEvent(cursor);
        cursor.close();

        return  newEvent;

    }

    public Long insertEvent(Event event){

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_EVENT_TYPE_ID, event.getType());
        values.put(DBHelper.COLUMN_EVENT_SOURCE_ID, event.getSource());
        values.put(DBHelper.COLUMN_EVENT_IDENTIFICATOR, event.getIdentificator());
        values.put(DBHelper.COLUMN_EVENT_DATETIME, event.getDatetime());
        values.put(DBHelper.COLUMN_EVENT_LOCATION, event.getLocation());
        values.put(DBHelper.COLUMN_EVENT_COMMENT, event.getComment());
        values.put(DBHelper.COLUMN_EVENT_STATUS, event.getStatus());
        values.put(DBHelper.COLUMN_EVENT_EMPLOYEE_ID, event.getEmployee().getId());
        values.put(DBHelper.COLUMN_EVENT_DEVICE_CODE, event.getDeviceCode());

        long result = db.insert(DBHelper.TABLE_EVENT, null, values);
        return result;
    }

    //Domyslnie 6 ostatnich
    public List<Event> getEventsOfEmployee(long id){
        return getEventsOfEmployee(id, "6");
    }
    public List<Event> getEventsOfEmployee(long id, String limit ){

        List<Event> listEvent = new ArrayList<Event>();
        Log.d(TAG, "getEventsOfEmployee id="+id);

        Cursor cursor = db.query(
                DBHelper.TABLE_EVENT,
                allColumn,
                DBHelper.COLUMN_EVENT_EMPLOYEE_ID + " = ? ",
                new String[]{String.valueOf(id)},
                null, null,
                "datetime DESC",
                limit
        );


            Log.d(TAG, "Count Event ="+cursor.getCount());
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
//                Event event = cursorToEvent(cursor);
//                listEvent.add(event);
                listEvent.add(cursorToEvent(cursor));
                cursor.moveToNext();
            }
            cursor.close();

        return listEvent;
    }

    public List<Event> getEventsWithStatus(int status){
        List<Event> listEvents = new ArrayList<Event>();

        Cursor cursor = db.query(
                DBHelper.TABLE_EVENT,
                allColumn,
                DBHelper.COLUMN_EVENT_STATUS + " = ? ",
                new String[]{ String.valueOf(status)},
                null,null,null);

        Log.d(TAG, "Count Events = "+cursor.getCount());
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            listEvents.add(cursorToEvent(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return listEvents;
    }

    public Event getLastEventOfEmployee(long id){
        Event event = new Event();

        Cursor cursor = db.query(
                DBHelper.TABLE_EVENT,
                allColumn,
                DBHelper.COLUMN_EVENT_EMPLOYEE_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,null,
                "datetime DESC",
                "1"
        );


        if(cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
        }
        cursor.close();
        return event;
    }

    public void deleteEvent(Event event){
        long id = event.getId();

        db.delete(DBHelper.TABLE_EVENT,DBHelper.COLUMN_EVENT_ID + " = " + id, null);
    }

    protected Event cursorToEvent(Cursor cursor){

        Event event = new Event();

        event.setId(cursor.getLong(0));
        event.setType(cursor.getInt(1));
        event.setSource(cursor.getInt(2));
        event.setIdentificator(cursor.getString(3));
        event.setDatetime(cursor.getString(4));
        event.setLocation(cursor.getString(5));
        event.setComment(cursor.getString(6));
        event.setStatus(cursor.getInt(7));
        event.setDeviceCode(cursor.getString(9)); //9 bo jest osatnio dodany

        //get Employee

        long employeeId = cursor.getLong(8);
        Log.d(TAG, "cursorToEvnet - EmployeeId = "+employeeId);

        EmployeeDAO employeeDAO = new EmployeeDAO(context);
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if(employee != null){
            event.setEmployee(employee);
        }

        return event;
    }

    public void deleteTable(){
        db.delete(DBHelper.TABLE_EVENT, null, null);
        Log.i(TAG, "Wyczyszono tabele Event");
    }
}
