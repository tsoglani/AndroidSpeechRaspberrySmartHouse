package com.wear.tsoglanakos.smartHouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;
import com.phillipcalvin.iconbutton.IconButton;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NewTimerActivity extends AppCompatActivity {
    private DatagramSocket clientSocket;
    boolean isReceiving = true;
    private Spinner devideIDSpinner, tab_command_text_view, command_mode;
    private ArrayList<String> commands_list = new ArrayList<String>();
    private ArrayList<String> ids_list = new ArrayList<String>();
    private Button sendButton;
    private TextView showTimeText;
    private Thread thread;
    private long timeStamp;
private IconButton countdown_timer;
    int sec=-1, hour=-1, min=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_timer);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        devideIDSpinner = (Spinner) findViewById(R.id.deviceID);
        tab_command_text_view = (Spinner) findViewById(R.id.tab_command_text_view);
        command_mode = (Spinner) findViewById(R.id.command_mode);
        sendButton = (Button) findViewById(R.id.save);
        showTimeText = (TextView) findViewById(R.id.showTimeText);
        sendButton.setEnabled(false);
        countdown_timer= (IconButton) findViewById(R.id.countdown_timer);
        timeStamp = System.currentTimeMillis();
        showTimeText.setText("no connected with the device, wait 3 seconds if this doesn't work, press reload icon (if this doesn't work again go back and open it again).");
        command_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        if (tab_command_text_view.getSelectedItem() != null && tab_command_text_view.getSelectedItem().toString() != null && !tab_command_text_view.getSelectedItem().toString().equals("")) {
                            showTimeText.setText("Execute \"" + tab_command_text_view.getSelectedItem().toString() + " " + command_mode.getSelectedItem().toString() + "\" command in " + getTimerText());
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tab_command_text_view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        if (tab_command_text_view.getSelectedItem() != null && tab_command_text_view.getSelectedItem().toString() != null && !tab_command_text_view.getSelectedItem().toString().equals("")) {
                            showTimeText.setText("Execute \"" + tab_command_text_view.getSelectedItem().toString() + " " + command_mode.getSelectedItem().toString() + "\" command in " + getTimerText());
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        devideIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

                                                  {
                                                      @Override
                                                      public void onItemSelected(AdapterView<?> parent, View view, int position,
                                                                                 long id) {
                                                          TextView textView = (TextView) view;
                                                          commands_list.removeAll(commands_list);
                                                          sendDataToAll("getCommandID" + textView.getText().toString());
                                                      }

                                                      @Override
                                                      public void onNothingSelected(AdapterView<?> parent) {
                                                          Log.e("position", "nothing selected");

                                                      }
                                                  }

        );
    }

    public void refreshFunction(View v) {

        if (thread == null || !thread.isAlive()) {
            receiver();
        }

        ids_list.removeAll(ids_list);
        sendDataToAll("getIDS");
    }

    private String getTimerText() {
if (hour==-1||min==-1||sec==-1){

    return "";
}

        String h = Integer.toString(hour), m = Integer.toString(min), s = Integer.toString(sec);
        if (h.length() == 1) {
            h = "0" + h;
        }
        if (m.length() == 1) {
            m = "0" + m;
        }
        if (s.length() == 1) {
            s = "0" + s;
        }
        return h + " : " + m + " : " + s;
    }

    private String getTimerText(int seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);
        String output = "";
//        if (day != 0) {
//            output = day + " days, ";
//        }
//        if (hours != 0) {
//            output = hours + " hours, ";
//        }
//        if (minute != 0)
//            output += minute + " minutes and ";
//        output += second + " seconds.";

        if (day != 0) {
            output = day + " days, ";
        }
//        if (hours != 0) {
        output = hours + " : ";
//        }
//        if (minute != 0)
        output += minute + " : ";
        output += second;
        return output;
    }

    public void goBack(View v) {
        onBackPressed();
    }

    public void saveFunction(View v) {

        if (hour==-1||min==-1||sec==-1) {
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                Toast.makeText(NewTimerActivity.this, "Enter time.", Toast.LENGTH_SHORT).show();
            }
        });
        return;
        }
            sendForActivation();
        // send to All the data
    }

    @Override
    protected void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
        sendDataToAll("getIDS");

    }

    @Override
    protected void onStop() {
        super.onStop();
        isReceiving = false;
        if (clientSocket != null) {
//            clientSocket.disconnect();
            clientSocket.close();
            clientSocket=null;
        }

    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
        Intent intent = new Intent(NewTimerActivity.this, TimerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
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

    private void receiver() {
        thread = new Thread() {
            @Override
            public void run() {
                while (isReceiving) {
                    byte[] receiveData = new byte[10024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null )
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
                        } else if (sentence.equals("newTimerOK")) {
                            isReceiving=false;
                            Intent intent = new Intent(NewTimerActivity.this, TimerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(intent);

                            toast("Timer added.");

                        }


                    } catch (SocketException e) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread.start();
    }

    private void toast(final String text) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(NewTimerActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
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

    private final String SENDING_TIME = "Time:";

    private void sendForActivation() {
        int sum=sec+min*60+hour*60*60;
        sendDataToAll("newTimer:DeviceID:" + devideIDSpinner.getSelectedItem().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.TIME_STAMP + Long.toString(timeStamp) + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_TEXT_STRING +
                tab_command_text_view.getSelectedItem().toString() + " " + command_mode.getSelectedItem().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + SENDING_TIME + Integer.toString(sum));
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
                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID + sendData).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }




    public void enterTimerFunction(View v){

        MyTimePickerDialog mTimePicker = new MyTimePickerDialog(this, new MyTimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                // TODO Auto-generated method stub
                sec=seconds;
                min=minute;
                hour=hourOfDay;
                runOnUiThread(new Thread(){
                    @Override
                    public void run() {
                        showTimeText.setText("Execute \"" + tab_command_text_view.getSelectedItem().toString() + " " + command_mode.getSelectedItem().toString() + "\" command in " + getTimerText());
                        countdown_timer.setText(getTimerText()+"   ");
                    }
                });

				/*time.setText(getString(R.string.time) + String.format("%02d", hourOfDay)+
						":" + String.format("%02d", minute) +
						":" + String.format("%02d", seconds));	*/
            }
        }, 0,1,0, true);
        mTimePicker.show();



    }
}