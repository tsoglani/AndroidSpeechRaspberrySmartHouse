package com.wear.tsoglanakos.smartHouse;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by tsoglani on 13/1/2016.
 */
public class DB_connectionHistory extends SQLiteOpenHelper {
    private Context context;
    private static String DATABASE_NAME = "Remote_Connection_history";
    public static final String TABLE_NAME = "table_history";
    public static final String IP = "ip";
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String PORT = "port";
    public DB_connectionHistory(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + TABLE_NAME + " " +
                        "(" + ID + " INTEGER PRIMARY KEY , " + IP + " text," + USERNAME + " text,"+ PORT + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertContact(String ip, String username,String port) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IP, ip);
        contentValues.put(USERNAME, username);
        contentValues.put(PORT, port);
        long insertedValue = db.insert(TABLE_NAME, null, contentValues);
    }


    public Cursor getData(String ip, String username,String port) {
        return getData(getID(ip, username,port));
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where id=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public boolean updateContact(String ip, String username,String port) {

        return updateContact(getID(ip, username,port), ip, username,port);
    }

    public boolean updateContact(int id, String ip, String username,String port) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(IP, ip);
        contentValues.put(USERNAME, username);
        contentValues.put(PORT, port);
        db.update(TABLE_NAME, contentValues, ID + " = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public int getID(String ip, String username,String port) {

        int id = -1;
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select " + ID + " from " + TABLE_NAME + " WHERE " + IP + "= ? AND " + USERNAME + "= ? AND "+PORT+ "= ?", new String[]{ip, username,port});
        if (res != null) {

            res.moveToFirst();
            id = res.getInt(0);
//            id=String.valueOf(recc);
        }

//        res.moveToFirst();
//        int counter = 1;
//        while (res.isAfterLast() == false) {
//            String dbIP = res.getString(res.getColumnIndex(IP));
//            String dbUsername = res.getString(res.getColumnIndex(USERNAME));
//            if (dbIP.equals(ip) && username.equals(dbUsername)) {
//                id = counter;
//            }
//            res.moveToNext();
//            counter++;
//        }
        return id;
    }


    public void deleteContact(final String ip, final String username,final String port) {

        String message = "Are you sure you want to remove it ?";

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteContact(getID(ip, username,port));
                        context.startActivity(new Intent(context, ConnectionHistory.class));

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();


    }

    public void deleteContactAndCreateAnotherOne(final String oldIp, final String oldUsername, final String newIp, final String newUsername,final String oldPort,final String newPort) {

        String message = "Are you sure you want to modify it ?";


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteContact(getID(oldIp, oldUsername,oldPort));
                        context.startActivity(new Intent(context, ConnectionHistory.class));
                        insertContact(newIp, newUsername,newPort);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();


    }


    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_NAME,
                ID + " = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllCotacts() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex(IP)) + "@@@" + res.getString(res.getColumnIndex(USERNAME))+"@@@" + res.getString(res.getColumnIndex(PORT)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean isStored(String ip, String username,String port) {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            String dbIP = res.getString(res.getColumnIndex(IP));
            String dbUsername = res.getString(res.getColumnIndex(USERNAME));

            String dbPort = res.getString(res.getColumnIndex(PORT));


            if (dbIP.equals(ip) && username.equals(dbUsername)&& port.equals(dbPort)) {
                return true;
            }
            res.moveToNext();
        }
        return false;
    }

}


