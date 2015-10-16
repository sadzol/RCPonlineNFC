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
import pl.rcponline.nfc.model.Employee;
import pl.rcponline.nfc.model.Event;

public class EmployeeDAO {

    public static final String TAG = "EmployeeDAO";

    //Database fields
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Context context;
    private String[] allColumns = { DBHelper.COLUMN_EMPLOYEE_ID ,
            DBHelper.COLUMN_EMPLOYEE_FIRSTNAME,
            DBHelper.COLUMN_EMPLOYEE_NAME,
            DBHelper.COLUMN_EMPLOYEE_PERMISSION
    };

    public EmployeeDAO(Context context){
        this.context = context;
        dbHelper = new DBHelper(context);
        //open database
        try{
            open();
        }catch (SQLException e){
            Log.e(TAG, "SQLExtension on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void open() throws SQLException{
        db = dbHelper.getWritableDatabase();
    }
    public void close(){
        dbHelper.close();
    }

    public Employee createEmployee(long id, String firstname, String name, int permission){
        Log.i(TAG, "Create Employee: " +
                        "employeeId: " + id +
                        ", firstName: " + firstname +
                        ", name: " + name +
                        ", permission: " + permission
        );

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_EMPLOYEE_ID, id);
        values.put(DBHelper.COLUMN_EMPLOYEE_FIRSTNAME, firstname);
        values.put(DBHelper.COLUMN_EMPLOYEE_NAME, name);
        values.put(DBHelper.COLUMN_EMPLOYEE_PERMISSION, permission);

        db.insert(DBHelper.TABLE_EMPLOYEE, null, values);
        //w przypadku autoincrement
//        Cursor cursor = db.query(DBHelper.TABLE_EMPLOYEE, allColumns, DBHelper.COLUMN_EMPLOYEE_ID + " = " + insertId, null, null, null,null);
        Cursor cursor = db.query(DBHelper.TABLE_EMPLOYEE, allColumns, DBHelper.COLUMN_EMPLOYEE_ID + " = " + id, null, null, null,null);

        cursor.moveToFirst();
        Employee newEmployee = cursorToEmployee(cursor);
        cursor.close();

        return newEmployee;
    }

    public void deleteEmployee(Employee employee){
        long id = employee.getId();

        //delete all event of this employee
        EventDAO eventDAO = new EventDAO(context);
        List<Event> listEvent = eventDAO.getEventsOfEmployee(id);
        if(listEvent != null && !listEvent.isEmpty()){
            for (Event e: listEvent){
                eventDAO.deleteEvent(e);
            }
        }

        db.delete(DBHelper.TABLE_EMPLOYEE, DBHelper.COLUMN_EMPLOYEE_ID + " = " + id, null);
        //todo usuna identyfikatory nalezace do pracownika
    }

    public List<Employee> getAllEmployees(){

        List<Employee> listEmployees = new ArrayList<Employee>();

        Cursor cursor = db.query(DBHelper.TABLE_EMPLOYEE, allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Employee employee = cursorToEmployee(cursor);
                listEmployees.add(employee);
                cursor.moveToNext();
            }

            //obowiazkowo zamknac cursor
            cursor.close();

            //SHORTER WAY
            //while(cursor.moveToNext()){
            //
            //}
        return listEmployees;
    }

    public Employee getEmployeeById(long id){

        Employee employee = new Employee();
        Log.d(TAG,"getEmployeeById id ="+id);

        Cursor cursor = db.query(
                DBHelper.TABLE_EMPLOYEE,
                allColumns,
                DBHelper.COLUMN_EMPLOYEE_ID + " = ? ",
                new String[]{String.valueOf(id)},
                null, null, null);

        if(cursor.moveToFirst()) {
            employee = cursorToEmployee(cursor);
        }

        cursor.close();
        return  employee;
    }

    protected Employee cursorToEmployee(Cursor cursor){
        Employee employee = new Employee();

        Log.d(TAG, "cursorToEmployee id="+cursor.getLong(0)+", firstName="+cursor.getString(1));
        employee.setId( cursor.getLong(0) );
        employee.setFirstname( cursor.getString(1) );
        employee.setName( cursor.getString(2) );
        employee.setPermission( cursor.getInt(3) );

        return employee;
    }

    public void deleteTable(){
        db.delete(DBHelper.TABLE_EMPLOYEE,null,null);
        Log.i(TAG,"Wyczyszono tabele Employee");
    }

}
