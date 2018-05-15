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
public class DB_SwitchCommand extends SQLiteOpenHelper {
    private Context context;
    private static String DATABASE_NAME = "DB_SwitchCommand";
    public static final String TABLE_NAME = "Switch_CMDS";
    public static final String COMMAND = "Command";
    public static final String ID = "id";

    public DB_SwitchCommand(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + TABLE_NAME + " " +
                        "(" + ID + " INTEGER PRIMARY KEY , " + COMMAND + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertContact(String command) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COMMAND, command);
        db.insert(TABLE_NAME, null, contentValues);
    }


    public Cursor getData(String oldCommand) {
        return getData(getID(oldCommand));
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

    public boolean updateContact(String command) {

        return updateContact(getID(command), command);
    }

    public boolean updateContact(int id, String command) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COMMAND, command);
        db.update(TABLE_NAME, contentValues, ID + " = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public int getID(String command) {

        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select " + ID + " from " + TABLE_NAME + " WHERE " + COMMAND + "= ?" , new String[]{command});
        if (res != null) {

            res.moveToFirst();
            id = res.getInt(0);
        }

        return id;
    }


    public void deleteContact(final String ip) {

        String message = "Are you sure you want to remove it ?";

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteContact(getID(ip));
                        Intent intent =new Intent(context, SwitchManualActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);

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

    public void deleteContactAndCreateAnotherOne(final String oldCommand, final String newCommand) {

        String message = "Are you sure you want to modify it ?";


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteContact(getID(oldCommand));
                        Intent intent =new Intent(context, SwitchManualActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        insertContact(newCommand);

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
            array_list.add(res.getString(res.getColumnIndex(COMMAND)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean isStored(String oldCommand) {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            String dbIP = res.getString(res.getColumnIndex(COMMAND));
            if (dbIP.equals(oldCommand) ) {
                return true;
            }
            res.moveToNext();
        }
        return false;
    }

}


