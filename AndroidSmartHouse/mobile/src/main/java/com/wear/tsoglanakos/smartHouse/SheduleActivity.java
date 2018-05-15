package com.wear.tsoglanakos.smartHouse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;

public class SheduleActivity extends AppCompatActivity {
    private DatagramSocket clientSocket;
    boolean isReceiving = true;
    private Button addNewSheduleButton;
    LinearLayout shedule_activity_linear_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shedule);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        init();
        shedule_activity_linear_layout.addView(addNewSheduleButton);
        sendDataToAll("getShedules");
        toast("wait 3 seconds to receive the data from device, if this doesn't work press refresh button.");
//        sendDataToAll("removeShedule0");
//        sendDataToAll("removeShedule2");
    }

    private void init() {
        addNewSheduleButton = new Button(this);
        // addNewSheduleButton.setText("Add new");
        addNewSheduleButton.setBackgroundResource(R.drawable.add_calentar);
        addNewSheduleButton.setLayoutParams(new LinearLayout.LayoutParams((int) pxFromDp(SheduleActivity.this, 50), (int) pxFromDp(SheduleActivity.this, 50)));
        addNewSheduleButton.setGravity(Gravity.CENTER);
        addNewSheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReceiving=false;
                Intent intent = new Intent(SheduleActivity.this, AddSceduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        shedule_activity_linear_layout = (LinearLayout) findViewById(R.id.shedule_activity_linear_layout);
    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
    }

    private void sendForUpdateSingleShedule(String deviceID, String commandID, String commandText, String sigleItemID, String sigleItemValue) {

        sendDataToAll("updateSingleShedule:" + AddSceduleActivity.DEVICE_ID + deviceID + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_ID + commandID + AddSceduleActivity.COMMAND_SPLIT_STRING +
                AddSceduleActivity.COMMAND_TEXT_STRING + commandText+  AddSceduleActivity.COMMAND_SPLIT_STRING+sigleItemID+sigleItemValue );
    }


    private void sendForUpdateShedule(String deviceID, String commandID, String commandText, String activeDays, String time, String weekly, String active) {

        sendDataToAll("updateShedule:" + AddSceduleActivity.DEVICE_ID + deviceID + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_ID + commandID + AddSceduleActivity.COMMAND_SPLIT_STRING +
                AddSceduleActivity.COMMAND_TEXT_STRING + commandText + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.DAYS_STRING + activeDays +
                AddSceduleActivity.COMMAND_SPLIT_STRING + "ActiveTime:"+ time + AddSceduleActivity.COMMAND_SPLIT_STRING
                + AddSceduleActivity.IS_WEEKLY + weekly + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.IS_ACTIVE + active);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        isReceiving = true;
//        receiver();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        isReceiving = true;
        receiver();
    }
    //    @Override
//    protected void onStop() {
//        super.onStop();
//        if (clientSocket != null ) {
//            clientSocket.disconnect();
//            clientSocket.close();
//        }
//        isReceiving = false;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientSocket != null ) {
//            clientSocket.disconnect();
            clientSocket.close();
            clientSocket=null;
        }
        isReceiving = false;
    }

    public void goBack(View v) {
        onBackPressed();
    }

    public void refreshFunction(View v) {
        toast("wait 3 seconds to receive the data from device");
        shedule_activity_linear_layout.removeAllViews();
        shedule_activity_linear_layout.addView(addNewSheduleButton);
        sendDataToAll("getShedules");
    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
        Intent intent = new Intent(this, Automation.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void receiver() {
        new Thread() {
            @Override
            public void run() {
                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null )
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        Log.e("sentence", sentence);

                        if (sentence.startsWith("Shedules:")) {

                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    shedule_activity_linear_layout.removeAllViews();
                                    shedule_activity_linear_layout.addView(addNewSheduleButton);

                                }
                            });
                            sentence = sentence.substring("Shedules:".length(), sentence.length());
                            if (sentence.length() < 17) {
                                toast("You have no running shedule.");
                                continue;
                            }


                            String deviceID = getDeviceID(sentence.split(AddSceduleActivity.COMMAND_SPLIT_STRING));
                            sentence = sentence.substring((AddSceduleActivity.DEVICE_ID + deviceID + AddSceduleActivity.COMMAND_SPLIT_STRING).length(), sentence.length());
                            Log.e("sentence", sentence);

                            String[] incomeShedules = sentence.split(AddSceduleActivity.SHEDULE_SPLIT_STRING);


                            for (int i = 0; i < incomeShedules.length; i++) {
                                String[] list = incomeShedules[i].split(AddSceduleActivity.COMMAND_SPLIT_STRING);
                                addTab(list, deviceID);
                            }


                        }
                        if (sentence.startsWith("newShedule")) { // otan kanw add ena command kai to dexonte aloi xristes .. diagrafw ola ta views me to device id kai vazw ta nea


                        }
                        if (sentence.startsWith("updatedOk") || sentence.startsWith("UpdatedOk")) {// elenxw to device id kai to command id kai kanw ta updates
                            sentence = sentence.substring("UpdatedOk:".length(), sentence.length());

                            String deviceID = getDeviceID(sentence.split(AddSceduleActivity.COMMAND_SPLIT_STRING));
                            sentence = sentence.substring((AddSceduleActivity.DEVICE_ID + deviceID + AddSceduleActivity.COMMAND_SPLIT_STRING).length(), sentence.length());
                            final String[] list = sentence.split(AddSceduleActivity.COMMAND_SPLIT_STRING);
                            onUpdated(sentence, deviceID);

                        }
                        if (sentence.startsWith("removeShedule")) {// elenxw to device id kai diagrafw to command id

                            String wantedCommand = sentence.substring("removeShedule:".length(), sentence.length());
                            final String[] receivedList = wantedCommand.split(AddSceduleActivity.COMMAND_SPLIT_STRING);
                            new AsyncTask<Void, Void, Void>() {
                                String deviceID, commandID;

                                @Override
                                protected Void doInBackground(Void... params) {
                                    deviceID = receivedList[0].substring(AddSceduleActivity.DEVICE_ID.length(), receivedList[0].length());
                                    commandID = receivedList[1].substring(AddSceduleActivity.COMMAND_ID.length(), receivedList[1].length());
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    checkToDelete(deviceID, commandID);
                                }
                            }.execute();

                        }


                    } catch (SocketException e) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }


    private void checkToDelete(String deviceID, String commandID) {
        for (int i = 0; i < shedule_activity_linear_layout.getChildCount(); i++) {
            View child = shedule_activity_linear_layout.getChildAt(i);
            Log.e(".getChildAt(i)", shedule_activity_linear_layout.getChildAt(i).toString());
            if (child instanceof RelativeLayout) {
                final RelativeLayout rl = (RelativeLayout) child;
                TextView device_id = (TextView) rl.findViewById(R.id.device_id);
                String devIdText = device_id.getText().toString();

                if (devIdText.substring("Device id:".length(), devIdText.length()).equals(deviceID) && rl.getTag().toString().equals(commandID)) {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            shedule_activity_linear_layout.removeView(rl);
                        }
                    });
                }

            }
        }

    }


    private void toast(final String text) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(SheduleActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendForRemoveShedule(String deviceID, String commandID, String commandText) {
        toast("wait 3 seconds to receive the data from device, if this doesn't work press refresh button.");
        String sendingText = "removeShedule:" + AddSceduleActivity.DEVICE_ID + deviceID + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_TEXT_STRING + commandID + AddSceduleActivity.COMMAND_SPLIT_STRING + commandText;
        //   Log.e("sendingText", sendingText);
        sendDataToAll(sendingText);
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void addTab(final String[] list, final String serverDeviceID) {


        final RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.shedule_tab, null);
        final TextView tab_command_text_view = (TextView) rl.findViewById(R.id.tab_command_text_view);
        final TextView time = (TextView) rl.findViewById(R.id.time);
        final Button tab_delete_button = (Button) rl.findViewById(R.id.tab_delete_button);
        final Button edit = (Button) rl.findViewById(R.id.edit);
        final TextView sunday_tab = (TextView) rl.findViewById(R.id.sunday_tab);
        final TextView monday_tab = (TextView) rl.findViewById(R.id.monday_tab);
        final TextView tuesday_tab = (TextView) rl.findViewById(R.id.tuesday_tab);
        final TextView wednesday_tab = (TextView) rl.findViewById(R.id.wednesday_tab);
        final TextView thursday_tab = (TextView) rl.findViewById(R.id.thursday_tab);
        final TextView friday_tab = (TextView) rl.findViewById(R.id.friday_tab);
        final TextView saturday_tab = (TextView) rl.findViewById(R.id.saturday_tab);
        final TextView device_id = (TextView) rl.findViewById(R.id.device_id);
        final CheckBox weekly = (CheckBox) rl.findViewById(R.id.weekly);
        final CheckBox active = (CheckBox) rl.findViewById(R.id.active);


//        String devideID=getDeviceID(list);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start AddSceduleActivity and put extra the parameters of curent tab view
                // on AddSceduleActivity side I will receive the String Data and if String Data !=null I will put them as default parameters to views
                // and instead of sending saveShedule I will send updateShadule and the data

                String activeDays = "";
                if (isDaySelected(sunday_tab)) {
                    String extgraString=(isDayOn(saturday_tab))?" on":" off";
                    activeDays += Calendar.SUNDAY+extgraString;
                }
                if (isDaySelected(monday_tab)) {
                    String extgraString=(isDayOn(monday_tab))?" on":" off";
                    activeDays += Calendar.MONDAY+extgraString;
                }
                if (isDaySelected(tuesday_tab)) {
                    String extgraString=(isDayOn(tuesday_tab))?" on":" off";
                    activeDays += Calendar.TUESDAY+extgraString;
                }
                if (isDaySelected(wednesday_tab)) {
                    String extgraString=(isDayOn(wednesday_tab))?" on":" off";
                    activeDays += Calendar.WEDNESDAY+extgraString;
                }
                if (isDaySelected(thursday_tab)) {
                    String extgraString=(isDayOn(thursday_tab))?" on":" off";
                    activeDays += Calendar.THURSDAY+extgraString;
                }
                if (isDaySelected(friday_tab)) {
                    String extgraString=(isDayOn(friday_tab))?" on":" off";
                    activeDays += Calendar.FRIDAY+extgraString;
                }
                if (isDaySelected(saturday_tab)) {
                    String extgraString=(isDayOn(saturday_tab))?" on":" off";
                    activeDays += Calendar.SATURDAY+extgraString;
                }
                isReceiving=false;
                Intent intent = new Intent(SheduleActivity.this, UpdateSceduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("extra", device_id.getText().toString().substring("Device id:".length(), device_id.getText().toString().length())
                        + AddSceduleActivity.COMMAND_SPLIT_STRING + rl.getTag().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + activeDays + AddSceduleActivity.COMMAND_SPLIT_STRING
                        + time.getText().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + weekly.isChecked() + AddSceduleActivity.COMMAND_SPLIT_STRING + active.isChecked() + AddSceduleActivity.COMMAND_SPLIT_STRING + tab_command_text_view.getText().toString());
//                defauldDeviceID = extraList[0];
//                defauldCommandID = extraList[1];
//                defaulActiveDays = extraList[2];
//                defaultTime = extraList[3];
//                defaultIsWeekly = extraList[4];
//                defaultIsActive = extraList[5];
                startActivity(intent);
            }
        });
        tab_delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String devIdText = device_id.getText().toString();
                sendForRemoveShedule(devIdText.substring("Device id:".length(), devIdText.length()), rl.getTag().toString(), tab_command_text_view.getText().toString());
            }
        });

        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("wait 3 seconds to send and receive the data from device");
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        active.setChecked(!active.isChecked());
                        String devIdText = device_id.getText().toString();
//                        String activeDays = "";
//                        if (isDaySelected(sunday_tab)) {
//                            String extgraString=(isDayOn(saturday_tab))?" on":" off";
//                            activeDays += Calendar.SUNDAY+extgraString;
//                        }
//                        if (isDaySelected(monday_tab)) {
//                            String extgraString=(isDayOn(monday_tab))?" on":" off";
//                            activeDays += Calendar.MONDAY+extgraString;
//                        }
//                        if (isDaySelected(tuesday_tab)) {
//                            String extgraString=(isDayOn(tuesday_tab))?" on":" off";
//                            activeDays += Calendar.TUESDAY+extgraString;
//                        }
//                        if (isDaySelected(wednesday_tab)) {
//                            String extgraString=(isDayOn(wednesday_tab))?" on":" off";
//                            activeDays += Calendar.WEDNESDAY+extgraString;
//                        }
//                        if (isDaySelected(thursday_tab)) {
//                            String extgraString=(isDayOn(thursday_tab))?" on":" off";
//                            activeDays += Calendar.THURSDAY+extgraString;
//                        }
//                        if (isDaySelected(friday_tab)) {
//                            String extgraString=(isDayOn(friday_tab))?" on":" off";
//                            activeDays += Calendar.FRIDAY+extgraString;
//                        }
//                        if (isDaySelected(saturday_tab)) {
//                            String extgraString=(isDayOn(saturday_tab))?" on":" off";
//                            activeDays += Calendar.SATURDAY+extgraString;
//                        }

                        //    private void sendForUpdateShedule(String deviceID,String commandID,String commandText,String activeDays,String time,String weekly,String active){

                        devIdText = devIdText.substring("Device id:".length(), devIdText.length());
//                        sendForUpdateShedule(devIdText, rl.getTag().toString(), tab_command_text_view.getText().toString(), activeDays, time.getText().toString(), Boolean.toString(weekly.isChecked()), Boolean.toString(!active.isChecked()));

//                        new AsyncTask<Void, Void, Void>() {
//                            @Override
//                            protected void onPreExecute() {
//                                weekly.setEnabled(false);
//                                active.setEnabled(false);
//                            }
//
//                            @Override
//                            protected Void doInBackground(Void... params) {
//                                try {
//                                    Thread.sleep(3000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                return null;
//                            }
//
//                            @Override
//                            protected void onPostExecute(Void aVoid) {
//                                weekly.setEnabled(true);
//                                active.setEnabled(true);
//                            }
//                        }.execute();

                        sendForUpdateSingleShedule(devIdText, rl.getTag().toString(), tab_command_text_view.getText().toString(),AddSceduleActivity.IS_ACTIVE,Boolean.toString(!active.isChecked()));
                    }
                });
            }
        });
        weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("wait 3 seconds to send and receive the data from device");
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        weekly.setChecked(!weekly.isChecked());
                        String devIdText = device_id.getText().toString();
//                        String activeDays = "";
//                        if (isDaySelected(sunday_tab)) {
//                            activeDays += Calendar.SUNDAY;
//                        }
//                        if (isDaySelected(monday_tab)) {
//                            activeDays += Calendar.MONDAY;
//                        }
//                        if (isDaySelected(tuesday_tab)) {
//                            activeDays += Calendar.TUESDAY;
//                        }
//                        if (isDaySelected(wednesday_tab)) {
//                            activeDays += Calendar.WEDNESDAY;
//                        }
//                        if (isDaySelected(thursday_tab)) {
//                            activeDays += Calendar.THURSDAY;
//                        }
//                        if (isDaySelected(friday_tab)) {
//                            activeDays += Calendar.FRIDAY;
//                        }
//                        if (isDaySelected(saturday_tab)) {
//                            activeDays += Calendar.SATURDAY;
//                        }

                        //    private void sendForUpdateShedule(String deviceID,String commandID,String commandText,String activeDays,String time,String weekly,String active){

                        devIdText = devIdText.substring("Device id:".length(), devIdText.length());
//                        sendForUpdateShedule(devIdText, rl.getTag().toString(), tab_command_text_view.getText().toString(), activeDays, time.getText().toString(), Boolean.toString(!weekly.isChecked()), Boolean.toString(active.isChecked()));
                        sendForUpdateSingleShedule(devIdText, rl.getTag().toString(), tab_command_text_view.getText().toString(),AddSceduleActivity.IS_WEEKLY,Boolean.toString(!weekly.isChecked()));

//                        new AsyncTask<Void, Void, Void>() {
//                            @Override
//                            protected void onPreExecute() {
//                                weekly.setEnabled(false);
//                                active.setEnabled(false);
//                            }
//
//                            @Override
//                            protected Void doInBackground(Void... params) {
//                                try {
//                                    Thread.sleep(3000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                return null;
//                            }
//
//                            @Override
//                            protected void onPostExecute(Void aVoid) {
//                                weekly.setEnabled(true);
//                                active.setEnabled(true);
//                            }
//                        }.execute();
                    }
                });
            }
        });
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    String commandID, commandText, daysString, timeString, isActive, isWeekly;

                    @Override
                    protected Void doInBackground(Void... params) {
                        commandID = getCommandID(list);
                        daysString = getDays(list);
                        timeString = getTime(list);
                        isActive = getIsActive(list);
                        isWeekly = getIsWeekly(list);
                        commandText = getCommandText(list);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        shedule_activity_linear_layout.addView(rl);
                        rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) pxFromDp(SheduleActivity.this, 150)));
                        tab_command_text_view.setText(commandText);
                        rl.setTag(commandID);
                        weekly.setChecked(Boolean.parseBoolean(isWeekly));
                        active.setChecked(Boolean.parseBoolean(isActive));
                        time.setText(timeString);
                        changeDayColor(sunday_tab, daysString, Calendar.SUNDAY);
                        changeDayColor(monday_tab, daysString, Calendar.MONDAY);
                        changeDayColor(tuesday_tab, daysString, Calendar.TUESDAY);
                        changeDayColor(wednesday_tab, daysString, Calendar.WEDNESDAY);
                        changeDayColor(thursday_tab, daysString, Calendar.THURSDAY);
                        changeDayColor(friday_tab, daysString, Calendar.FRIDAY);
                        changeDayColor(saturday_tab, daysString, Calendar.SATURDAY);
                        device_id.setText("Device id:" + serverDeviceID);
                        shedule_activity_linear_layout.removeView(addNewSheduleButton);
                        shedule_activity_linear_layout.addView(addNewSheduleButton);

                    }
                }.execute();


            }
        });

    }

    private void changeDayColor(View v, String daysString, int day) {
        if (daysString.contains(Integer.toString(day)+" on")) {
            v.setBackgroundColor(getResources().getColor(R.color.green));
        } else   if (daysString.contains(Integer.toString(day)+" off")) {
            v.setBackgroundColor(getResources().getColor(R.color.red));
        }else   {
            v.setBackgroundColor(getResources().getColor(R.color.gray));
        }
    }
    private boolean isDayOn(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.green)) {
                return true;
            }

        }

        return false;
    }

    private void onUpdated(String command, final String serverDeviceID) {


        for (int i = 0; i < shedule_activity_linear_layout.getChildCount(); i++) {
            View child = shedule_activity_linear_layout.getChildAt(i);

            if (child instanceof RelativeLayout) {
                final RelativeLayout rl = (RelativeLayout) child;
                final TextView tab_command_text_view = (TextView) rl.findViewById(R.id.tab_command_text_view);
                final TextView time = (TextView) rl.findViewById(R.id.time);
                final TextView sunday_tab = (TextView) rl.findViewById(R.id.sunday_tab);
                final TextView monday_tab = (TextView) rl.findViewById(R.id.monday_tab);
                final TextView tuesday_tab = (TextView) rl.findViewById(R.id.tuesday_tab);
                final TextView wednesday_tab = (TextView) rl.findViewById(R.id.wednesday_tab);
                final TextView thursday_tab = (TextView) rl.findViewById(R.id.thursday_tab);
                final TextView friday_tab = (TextView) rl.findViewById(R.id.friday_tab);
                final TextView saturday_tab = (TextView) rl.findViewById(R.id.saturday_tab);
                final TextView device_id = (TextView) rl.findViewById(R.id.device_id);
                final CheckBox weekly = (CheckBox) rl.findViewById(R.id.weekly);
                final CheckBox active = (CheckBox) rl.findViewById(R.id.active);
                final String[] list = command.split(AddSceduleActivity.COMMAND_SPLIT_STRING);

                final String commandID = getCommandID(list);
                if (!commandID.equals(rl.getTag().toString())
                        || !serverDeviceID.equals(device_id.getText().toString().substring("Device id:".length(), device_id.getText().toString().length()))) {
                    continue;
                }
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        new AsyncTask<Void, Void, Void>() {
                            String commandText, daysString, timeString, isActive, isWeekly;

                            @Override
                            protected Void doInBackground(Void... params) {

                                daysString = getDays(list);
                                timeString = getTime(list);
                                isActive = getIsActive(list);
                                isWeekly = getIsWeekly(list);
                                commandText = getCommandText(list);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) pxFromDp(SheduleActivity.this, 150)));
                                tab_command_text_view.setText(commandText);
                                rl.setTag(commandID);
                                weekly.setChecked(Boolean.parseBoolean(isWeekly));
                                active.setChecked(Boolean.parseBoolean(isActive));
                                time.setText(timeString);
                                changeDayColor(sunday_tab, daysString, Calendar.SUNDAY);
                                changeDayColor(monday_tab, daysString, Calendar.MONDAY);
                                changeDayColor(tuesday_tab, daysString, Calendar.TUESDAY);
                                changeDayColor(wednesday_tab, daysString, Calendar.WEDNESDAY);
                                changeDayColor(thursday_tab, daysString, Calendar.THURSDAY);
                                changeDayColor(friday_tab, daysString, Calendar.FRIDAY);
                                changeDayColor(saturday_tab, daysString, Calendar.SATURDAY);
                                device_id.setText("Device id:" + serverDeviceID);
                                shedule_activity_linear_layout.removeView(addNewSheduleButton);
                                shedule_activity_linear_layout.addView(addNewSheduleButton);

                            }
                        }.execute();


                    }
                });

            }

        }
    }

    private boolean isDaySelected(View v) {
        int color = Color.TRANSPARENT;
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
            if (color == getResources().getColor(R.color.green)||color == getResources().getColor(R.color.red)) {
                return true;
            }

        }

        return false;
    }


    private String getDeviceID(String[] list) {
        String out = list[0];
        Log.e("getDeviceID", out);
        return out.substring(AddSceduleActivity.DEVICE_ID.length(), out.length());
    }

    private String getCommandID(String[] list) {
        String out = list[0];
        Log.e("getCommandID", out);
        return out.substring(AddSceduleActivity.COMMAND_ID.length(), out.length());
    }

    private String getCommandText(String[] list) {
        String out = list[1];
        Log.e("getCommandID", out);
        return out.substring(AddSceduleActivity.COMMAND_TEXT_STRING.length(), out.length());
    }

    private String getDays(String[] list) {
        String out = list[2];
        Log.e("getDays", out);

        return out.substring(AddSceduleActivity.DAYS_STRING.length(), out.length());
    }

    private String getTime(String[] list) {
        String out = list[3];
        Log.e("getTime", out);

        return out.substring("ActiveTime:".length(), out.length());
    }

    private String getIsWeekly(String[] list) {
        String out = list[4];
        Log.e("getIsWeekly", out);
        return out.substring(AddSceduleActivity.IS_WEEKLY.length(), out.length());
    }

    private String getIsActive(String[] list) {
        String out = list[5];
        Log.e("getIsActive", out);
        return out.substring(AddSceduleActivity.IS_ACTIVE.length(), out.length());
    }

    private void sendData(final String output, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String    sendData= output;
                    try {
                        sendData=  StringUtils.stripAccents(output);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID+sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID+sendData).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

}