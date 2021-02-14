package com.goel.attendancetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.goel.attendancetracker.classes.ClassesModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class DatabaseHandler extends SQLiteOpenHelper {


    public DatabaseHandler(Context context) {
        super(context, Params.DB_NAME, null, Params.DB_VERSION);
        this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + Params.ROOT_TABLE;
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void deleteOrganisation(String organisationId, String organisationName)
    {
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.delete(Params.ORGANISATIONS, "s_no=?", new String[]{organisationId});
        writableDatabase.execSQL("DROP TABLE IF EXISTS " + "\"" + organisationName + "\"");
        writableDatabase.close();
    }

    public void updateOrganisation(ContentValues values, String organisationId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.update(Params.ORGANISATIONS, values, "s_no=?", new String[]{organisationId});
        writableDatabase.close();
    }

    public void createNewOrganisation(OrganisationsDataModel newOrganisation){
        try (SQLiteDatabase writableDatabase = this.getWritableDatabase()) {
            String tableCommand = "CREATE TABLE " + "\"" + newOrganisation.getName() + "\"" + Params.NEW_TABLE;
            writableDatabase.execSQL(tableCommand);
        }
        catch (SQLiteException e){
            throw new SQLiteException();
        }
    }

    public void insertOrganisation(ContentValues values){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.insert(Params.ORGANISATIONS, null, values);
        writableDatabase.close();
    }

    // CLASSES METHOD

    public void deleteClass(String organisationName, String classId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.delete("\"" + organisationName + "\"", "class_sno=?", new String[]{classId});
        writableDatabase.close();
    }

    public void addNewClass(String name, ContentValues values){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.insert( "\"" + name + "\"", null, values);
        writableDatabase.close();
    }

    public void updateClass(String organisationName, ContentValues values, String classId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.update("\"" + organisationName + "\"", values, "class_sno=?", new String[]{classId});
        writableDatabase.close();
    }

    public void markAttendance(String organisationName, ClassesModel model, String markDate, int[] attendance){

        JSONObject history;
        try {
            history = new JSONObject(model.getClassHistory());
        } catch (JSONException e) {
            return;
        }
        // INITIALIZING INITIAL DATE HISTORY
        JSONArray dateHistory;
        try {
            dateHistory = (JSONArray) history.get(markDate);
        } catch (JSONException e) {
            dateHistory = new JSONArray();
            try {
                dateHistory.put(0,0);
                dateHistory.put(1,0);
            } catch (JSONException jsonException) {
                return;
            }
        }

        // UPDATE MODEL HISTORY
        try {
            dateHistory.put(0, attendance[0] + dateHistory.getInt(0));
            dateHistory.put(1, attendance[1] + dateHistory.getInt(1));
        } catch (JSONException e) {
            return;
        }

        try {
            history.put(markDate, dateHistory);
            model.setClassHistory(history.toString());
        } catch (JSONException e) {
            return;
        }

        // UPDATE NEW ATTENDANCE
        int present = 0;
        int absent = 0;
        try {
            Iterator<String> keys = history.keys();
            while (keys.hasNext()){
                JSONArray dateData = (JSONArray) history.get(keys.next());
                present += dateData.getInt(0);
                absent += dateData.getInt(1);
            }
        }catch (JSONException e) {
            return;
        }

        int newAttendance = 100;
        if (present != 0 || absent != 0){
            newAttendance = (present * 100)/(present + absent);
        }
        model.setClassAttendancePercentage(newAttendance);

        // UPDATE IN DATABASE
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Params.HISTORY, history.toString());
        values.put(Params.ATTENDANCE, newAttendance);
        this.updateClass(organisationName, values, String.valueOf(model.getId()));
        writableDatabase.close();
    }

}
