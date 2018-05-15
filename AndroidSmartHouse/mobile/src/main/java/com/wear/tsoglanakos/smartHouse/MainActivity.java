package com.wear.tsoglanakos.smartHouse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static String UNIQUE_USER_ID = null;
    protected static final String UNIQUE_USER_ID_SPLIT = "!!!!!";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        generateUniqueUserID();
        AutoConnection.port = receiveInt("port", 2222);


        Log.e("eeeeeeeee","oooooooo");
    }

    public void connectFunction(final View v) {
        if (!isNetworkAvailable()) {
            createAndShowAlertDialog();
            return;
        }

        final AutoConnection autoConnection = new AutoConnection(this, true);
        new Thread() {
            @Override
            public void run() {
                try {
                    autoConnection.sendToAllIpInNetwork();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        v.setEnabled(false);
                        v.setBackgroundResource(R.drawable.connection_1);
                    }
                });
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(v!=null)
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            v.setEnabled(true);
                            v.setBackgroundResource(R.drawable.connection);
                        }
                    });
            }
        }.execute();
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auto connection .. be sure you are in same local network with the server device.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Wifi connection.");
        builder.setMessage("You must be connected to Wifi network.Do you want to open Wifi settings ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
                openWifiSettings();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void openWifiSettings() {
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void manualIPConnection(View v) {
        Intent intent=new Intent(MainActivity.this, ConnectionHistory.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

    }

    public void portChangeFunction(View v) {
        final EditText input = new EditText(MainActivity.this);
        input.setText(Integer.toString(AutoConnection.port));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        input.setGravity(Gravity.CENTER);
        builder.setTitle("Change port.");
        builder.setMessage("Do you want to change Port?\nDefault port is 2222, change it only if you don't want to use default java file on raspberry side (see descriptions).");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                try {
                    storeInt("port", Integer.parseInt(input.getText().toString()));
                    AutoConnection.port = Integer.parseInt(input.getText().toString());
                } catch (Exception e) {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Enter valid port number.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialog.setView(input); // uncomment this line
        dialog.show();
    }


    private void storeInt(String dataID, int data) {
        SharedPreferences.Editor editor = getSharedPreferences(SwitchManualActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(dataID, data);
        editor.commit();
    }

    private int receiveInt(String dataID, int defaultReturningString) {

        SharedPreferences prefs = getSharedPreferences(SwitchManualActivity.MY_PREFS_NAME, MODE_PRIVATE);
        int restoredText = prefs.getInt(dataID, defaultReturningString);
        return restoredText;

    }
    public static void generateUniqueUserID() {
        if (UNIQUE_USER_ID == null) {
            UNIQUE_USER_ID = "userUniqueID:" + UUID.randomUUID().toString() + UNIQUE_USER_ID_SPLIT;
        }
    }

}
