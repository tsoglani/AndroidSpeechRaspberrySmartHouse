package com.wear.tsoglanakos.smartHouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.phillipcalvin.iconbutton.IconButton;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.igenius.customcheckbox.CustomCheckBox;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;

public class AddSceduleActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private Spinner devideIDSpinner, tab_command_text_view;
    private ArrayList<String> ids_list = new ArrayList<String>();
    private ArrayList<String> commands_list = new ArrayList<String>();
    private  IconButton time_text;
    private TextView sunday_tab, monday_tab, tuesday_tab, wednesday_tab, thursday_tab, friday_tab, saturday_tab;
    private CheckBox weekly, active;
    private Button sendButton;
    public final static String DAYS_STRING = "ActiveDays:", TIME_STRING = "Time:",ACTIVE_TIME_STRING = "ActiveTime:", COMMAND_TEXT_STRING = "CommandText:",TIME_STAMP="TimeStamp:",
            IS_WEEKLY = "IsWeekly:", IS_ACTIVE = "IsActive:", COMMAND_SPLIT_STRING = "##", SHEDULE_SPLIT_STRING = "@@!@@", COMMAND_ID = "CommandID:", DEVICE_ID = "DeviceID:",
            MULTY_TIMERS_STRING ="@@TimerSeperator@@";




    private GradientDrawable gray,green,red;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scedule);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        devideIDSpinner = (Spinner) findViewById(R.id.deviceID);
        tab_command_text_view = (Spinner) findViewById(R.id.tab_command_text_view);
        time_text= (IconButton) findViewById(R.id.countdown_timer);
        sunday_tab = (TextView) findViewById(R.id.sunday_tab);
        monday_tab = (TextView) findViewById(R.id.monday_tab);
        tuesday_tab = (TextView) findViewById(R.id.tuesday_tab);
        wednesday_tab = (TextView) findViewById(R.id.wednesday_tab);
        thursday_tab = (TextView) findViewById(R.id.thursday_tab);
        friday_tab = (TextView) findViewById(R.id.friday_tab);
        saturday_tab = (TextView) findViewById(R.id.saturday_tab);




        active = (CheckBox) findViewById(R.id.active);
        sendButton = (Button) findViewById(R.id.save);
        weekly = (CheckBox) findViewById(R.id.weekly);
        sendButton.setEnabled(false);
        devideIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                commands_list.removeAll(commands_list);
                sendDataToAll("getCommandID" + textView.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("position", "nothing selected");

            }
        });

        gray=getShape(Color.GRAY);
        green=getShape(Color.GREEN);
        red=getShape(Color.RED);

        sunday_tab.setBackground(gray);
        monday_tab.setBackground(gray);
        tuesday_tab.setBackground(gray);
        wednesday_tab.setBackground(gray);
        thursday_tab.setBackground(gray);
        friday_tab.setBackground(gray);
        saturday_tab.setBackground(gray);
    }

    private GradientDrawable getShape(int color){

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[] { 20, 20, 20, 20, 0, 0, 0, 0 });
        shape.setColor(color);
        shape.setCornerRadius(20);
        shape.setStroke(3, getResources().getColor(R.color.Teal));

    return shape;
    }


    public void goBack(View v) {
        onBackPressed();
    }



    @Override
    protected void onStop() {
        super.onStop();
        isReceiving = false;
        if (clientSocket != null ) {
//            clientSocket.disconnect();
            clientSocket.close();
            clientSocket=null;
        }

    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
        Intent intent = new Intent(this, SheduleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setSpinnerItem(final Spinner spinner, final ArrayList<String> strings) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.my_spinner_text, strings);
//                adapter.setDropDownViewResource(R.layout.my_spinner_text);
                spinner.setAdapter(adapter);
            }
        });

    }

    private boolean isReceiving = true;
    private DatagramSocket clientSocket;
    private Thread thread;

    private void receiver() {
        thread = new Thread() {

            @Override
            public void run() {
                ids_list.removeAll(ids_list);
//                ids_list.add("select Device ID");

                while (isReceiving) {
                    byte[] receiveData = new byte[10024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        Log.e("sentence", sentence);

                        if (sentence.startsWith("getIDS")) {
                            sentence = sentence.substring("getIDS".length(), sentence.length());
                            ids_list.add(sentence);
                            setSpinnerItem(devideIDSpinner, ids_list);

                        } else if (sentence.startsWith("getComandID")) {
                            sentence = sentence.substring("getComandID".length(), sentence.length());
                            proccessCommandsAndAddToList(sentence);
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    sendButton.setEnabled(true);
                                }
                            });
                        } else if (sentence.equals("addedOk")) {
                            isReceiving=false;
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddSceduleActivity.this, "New shedule saved.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(AddSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(intent);
                        } else if (sentence.equals("addedNotOk")) {
                            isReceiving=false;
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddSceduleActivity.this, "This shedule already exists.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(AddSceduleActivity.this, SheduleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }


                    } catch (SocketException e) {
//                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                thread=null;

            }
        };
        thread.start();
    }

    private void proccessCommandsAndAddToList(String commands) {
        commands_list.removeAll(commands_list);
        String[] list = commands.split("@@@");
        for (String str : list) {
            if (str.endsWith(" on")) {
                str = str.substring(0, str.length() - " on".length());
            } else if (str.endsWith(" off")) {
                str = str.substring(0, str.length() - " off".length());
            } else if (str.endsWith("on")) {
                str = str.substring(0, str.length() - "on".length());
            } else if (str.endsWith("off")) {
                str = str.substring(0, str.length() - "off".length());
            }
            commands_list.add(str);
        }
        setSpinnerItem(tab_command_text_view, commands_list);

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

    public void refreshFunction(View v) {

        if (thread == null || !thread.isAlive()) {
            receiver();
        }

        ids_list.removeAll(ids_list);
        sendDataToAll("getIDS");
    }


   



    public void saveFunction(final View v) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        v.setEnabled(false);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        if (v != null) {
                            v.setEnabled(true);
                        }
                    }
                }.execute();
            }
        });

        String activeDays = "";
        if (isDaySelected(sunday_tab)) {
            String extgraString=(isDayOn(sunday_tab))?" on":" off";
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

        try {

            sendDataToAll("saveShedule:DeviceID:" + devideIDSpinner.getSelectedItem().toString() + COMMAND_SPLIT_STRING + COMMAND_TEXT_STRING + tab_command_text_view.getSelectedItem().toString() +
                    COMMAND_SPLIT_STRING + DAYS_STRING + activeDays + COMMAND_SPLIT_STRING + ACTIVE_TIME_STRING + time_text.getText().toString().replaceAll(" ","")
                    + COMMAND_SPLIT_STRING + IS_WEEKLY + weekly.isChecked() + COMMAND_SPLIT_STRING + IS_ACTIVE + active.isChecked());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isDaySelected(View v) {
        Drawable background = v.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable color= (GradientDrawable) background;

            if (  color == red) {
                return true;
            }
            if (  color == green) {
                return true;
            }

        }

        return false;
    }

    private boolean isDayOn(View v) {
        Drawable background = v.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable color= (GradientDrawable) background;
            if (color ==green) {
                return true;
            }

        }

        return false;
    }
//    private boolean isDayOff(View v) {
//        int color = Color.TRANSPARENT;
//        Drawable background = v.getBackground();
//        if (background instanceof ColorDrawable) {
//            color = ((ColorDrawable) background).getColor();
//            if (color == getResources().getColor(R.color.red)) {
//                return true;
//            }
//
//        }
//
//        return false;
//    }


    public void chooseDayFunction(View v) {
        Drawable background = v.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable color= (GradientDrawable) background;

            if (color == green) {
                v.setBackground(red);
            } else if (color == red) {
                v.setBackground(gray);

            } else if (color ==gray) {
                v.setBackground(green);

            }


        }
    }



    private TimePickerDialog tpd;
    public void timeToUpdateFunction(View v){
        Calendar now = Calendar.getInstance();
                /*
                It is recommended to always create a new instance whenever you need to show a Dialog.
                The sample app is reusing them because it is useful when looking for regressions
                during testing
                 */


        if (tpd == null) {
            tpd = TimePickerDialog.newInstance(
                    AddSceduleActivity.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    now.get(Calendar.SECOND),
                    true
            );
        } else {
            tpd.initialize(
                    AddSceduleActivity.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    now.get(Calendar.SECOND),
                   true
            );
        }
//        now.clear();

        tpd.setThemeDark(true);
//        tpd.vibrate(vibrateTime.isChecked());
        tpd.dismissOnPause(true);
        tpd.enableSeconds(true);
//        tpd.setVersion(showVersion2.isChecked() ? TimePickerDialog.Version.VERSION_2 : TimePickerDialog.Version.VERSION_1);

        tpd.setVersion(TimePickerDialog.Version.VERSION_2 );
        tpd.setAccentColor(Color.parseColor("#9C27B0"));

//        if (titleTime.isChecked()) {
            tpd.setTitle("Choose Time");
//        }



        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
        tpd.show(getFragmentManager(), "Timepickerdialog");

    }

    @Override
    public void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
        sendDataToAll("getIDS");

        TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("Timepickerdialog");
        if(tpd != null) tpd.setOnTimeSetListener(this);


    }






    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String secondString = second < 10 ? "0"+second : ""+second;
        String time = hourString+":"+minuteString+":"+secondString+"   ";
        time_text.setText(time);
    }

}