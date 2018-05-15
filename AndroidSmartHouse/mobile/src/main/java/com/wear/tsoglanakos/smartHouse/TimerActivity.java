package com.wear.tsoglanakos.smartHouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TimerActivity extends AppCompatActivity {
    private DatagramSocket clientSocket;
    boolean isReceiving = true;
    private Button addNewTimerButton;
    LinearLayout timer_activity_linear_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        timer_activity_linear_layout = (LinearLayout) findViewById(R.id.timer_activity_linear_layout);
        sendDataToAll("getTimers");
        toast("wait 3 seconds to receive the active Timers from device");

        addNewTimerButton = new Button(this);
        addNewTimerButton.setBackgroundResource(R.drawable.add_timer);
        addNewTimerButton.setLayoutParams(new LinearLayout.LayoutParams((int) SheduleActivity.pxFromDp(TimerActivity.this, 50), (int) SheduleActivity.pxFromDp(TimerActivity.this, 50)));
        addNewTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReceiving=false;
                Intent intent = new Intent(TimerActivity.this, NewTimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        timer_activity_linear_layout.addView(addNewTimerButton);
    }

    private void sendDataToAll(String text) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData(text, inetAddress, AutoConnection.port);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isReceiving = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        isReceiving = true;
//        try {
//            clientSocket = new DatagramSocket();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        receiver();
    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (clientSocket != null) {
//            clientSocket.disconnect();
//            clientSocket.close();
//            clientSocket=null;
//        }
//        isReceiving = false;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientSocket != null) {
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
        toast("wait 3 seconds to receive data from device");
        if(clientSocket!=null)
        clientSocket.close();
                        clientSocket=null;
        receiver();

        timer_activity_linear_layout.removeAllViews();
        timer_activity_linear_layout.addView(addNewTimerButton);
        sendDataToAll("getTimers");
    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
        Intent intent = new Intent(this, Automation.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void toast(final String text) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(TimerActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
Thread thread;
    private void receiver() {
//        if(thread==null){

        thread=  new Thread() {
            @Override
            public void run() {
                isReceiving = true;
                while (isReceiving) {
                    try {

                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);

                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        Log.e("sentence", sentence);

                        if (sentence.startsWith("Timers:")) {
                            runOnUiThread(new Thread(){
                                @Override
                                public void run() {
                                    timer_activity_linear_layout.removeAllViews();

                                    timer_activity_linear_layout.addView(addNewTimerButton);

                                }
                            });
                            sentence = sentence.substring("Timers:".length());


                            if (sentence.length() < 20) {
                                toast("Timer is Empty");
                                continue;
                            }

                            String DeviceID = sentence.split(AddSceduleActivity.COMMAND_SPLIT_STRING)[0].substring(AddSceduleActivity.DEVICE_ID.length());

                            sentence = sentence.substring((AddSceduleActivity.DEVICE_ID + DeviceID + AddSceduleActivity.COMMAND_SPLIT_STRING).length());
                        //    Log.e("sentence before", sentence);

                            String[] receivedTimers = sentence.split(AddSceduleActivity.SHEDULE_SPLIT_STRING);
                            for (int i = 0; i < receivedTimers.length; i++) {

                                String curTimer = receivedTimers[i];
                              //  Log.e("sentence", curTimer);

                                String[] timerElements = curTimer.split(AddSceduleActivity.COMMAND_SPLIT_STRING);
                                String
                                        commandID = timerElements[0].substring(AddSceduleActivity.COMMAND_ID.length(), timerElements[0].length()),
                                        timeStamp = timerElements[1].substring(AddSceduleActivity.TIME_STAMP.length(), timerElements[1].length()),
                                        command = timerElements[2].substring(AddSceduleActivity.COMMAND_TEXT_STRING.length(), timerElements[2].length()),
                                        remainingTime = timerElements[3].substring(AddSceduleActivity.TIME_STRING.length(), timerElements[3].length());

                         //       Log.e("sentence " + i, "DeviceID " + DeviceID + ", commandID=" + commandID + ", timeStamp= " + timeStamp + ", command= " + command + " ,remainingTime=" + remainingTime);

                                addItems(DeviceID, commandID, timeStamp, command, remainingTime);
                            }


                        }else if(sentence.startsWith("removeTimer:")){
                            // removeTimer:DeviceID:0CommandID:0##TimeStamp:1457625771345##CommandText:kitchen lights on
                            String UsingCommand=sentence.substring("removeTimer:".length(),sentence.length());

                            String [] list=UsingCommand.split(AddSceduleActivity.COMMAND_SPLIT_STRING);
                            String device_id=list[0].substring(AddSceduleActivity.DEVICE_ID.length());

                            String commandID=list[1].substring(AddSceduleActivity.COMMAND_ID.length());
                            String timeStamp=list[2].substring(AddSceduleActivity.TIME_STAMP.length());
                            String command_text=list[3].substring(AddSceduleActivity.COMMAND_TEXT_STRING.length());


                            removeItem(device_id,commandID,timeStamp,command_text);

                        }

                    }catch (SocketException e){
                        e.printStackTrace();
                        if(clientSocket!=null)
                        clientSocket.close();
                        clientSocket=null;
                        break;
                    }catch (Exception|Error e) {
                        e.printStackTrace();
                        if(clientSocket!=null)
                            clientSocket.close();
                        clientSocket=null;
                        break;
                    }

                }

                thread=null;
Log.e("Stop Thread","Receiver");

            }

        };

        thread.start();
//    }
    }

    private void removeItem(final String DeviceID, final String commandID, final String timeStamp, final String command){

        for(int i=0;i<timer_activity_linear_layout.getChildCount();i++){
            View v=timer_activity_linear_layout.getChildAt(i);
            if(v instanceof RelativeLayout){
              final  RelativeLayout rl= (RelativeLayout) v;
                final TextView command_text_timer_tab = (TextView) rl.findViewById(R.id.command_text_timer_tab);
                final TextView device_id_text_timer_tab = (TextView) rl.findViewById(R.id.device_id_text_timer_tab);
                if(rl.getTag().toString().equals(AddSceduleActivity.COMMAND_ID + commandID + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.TIME_STAMP + timeStamp)
                        &&command_text_timer_tab.getText().toString().equals(command)&&device_id_text_timer_tab.getText().toString().substring("Device id: ".length()).equals(DeviceID)){
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            timer_activity_linear_layout.removeView(rl);

                        }
                    });
                }
            }
        }

    }

    private void addItems(final String DeviceID, final String commandID, final String timeStamp, final String command, final String remainingTime) {
        final RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.timer_tab, null);
        final TextView command_text_timer_tab = (TextView) rl.findViewById(R.id.command_text_timer_tab);
        final TextView device_id_text_timer_tab = (TextView) rl.findViewById(R.id.device_id_text_timer_tab);
        final TextView time_text_timer_tab = (TextView) rl.findViewById(R.id.time_text_timer_tab);
        final Button delete_button_timer_tab = (Button) rl.findViewById(R.id.delete_button_timer_tab);
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                command_text_timer_tab.setText(command);
                device_id_text_timer_tab.setText("Device id: " + DeviceID);
                time_text_timer_tab.setText("Remaining time: " + remainingTime);
                rl.setTag(AddSceduleActivity.COMMAND_ID + commandID + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.TIME_STAMP + timeStamp);
                delete_button_timer_tab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendDataToAll("removeTimer:" + AddSceduleActivity.DEVICE_ID + DeviceID +AddSceduleActivity.COMMAND_SPLIT_STRING+ rl.getTag().toString() + AddSceduleActivity.COMMAND_SPLIT_STRING + AddSceduleActivity.COMMAND_TEXT_STRING + command);
                    }
                });
                timer_activity_linear_layout.removeView(addNewTimerButton);

                timer_activity_linear_layout.addView(rl);


                timer_activity_linear_layout.addView(addNewTimerButton);

                new Thread() {
                    @Override
                    public void run() {

                        int time = Integer.parseInt(remainingTime);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        while (time > 0 &&rl.getParent()!=null) {//&& timer_activity_linear_layout.findViewById(rl.getId()) != null
                            try {
                                Thread.sleep(1000);
                                time--;
                                final int finalTime = time;
                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        time_text_timer_tab.setText("Remaining time: " + finalTime);
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        }

                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {
                                timer_activity_linear_layout.removeView(rl);

                            }
                        });
                    }
                }.start();


            }
        });

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
                    if (clientSocket == null )
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
