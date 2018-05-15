package com.wear.tsoglanakos.smartHouse;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MenuActivity extends AppCompatActivity {
    private DatagramSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MainActivity.generateUniqueUserID();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isReceiving = true;
        receiver();
    }

    public void chooseSpeechFunction(View v) {

        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseSpeechFunction", inetAddress, AutoConnection.port);
        }

    }

    public void automationFunction(View v) {


        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseAutomationFunction", inetAddress, AutoConnection.port);
        }

    }

    public void chooseSwitchFunction(View v) {
        for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
            sendData("chooseSwitchFunction", inetAddress, AutoConnection.port);
        }


    }

    @Override
    public void onBackPressed() {
        isReceiving=false;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goBack(View v) {
        onBackPressed();
    }

    private boolean isReceiving = true;

    private void receiver() {
        new Thread() {
            @Override
            public void run() {
                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        if (sentence.equalsIgnoreCase("chooseSwitchFunction")) {
                            isReceiving=false;
                            Intent intent = new Intent(MenuActivity.this, SwitchManualActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else if (sentence.equalsIgnoreCase("chooseSpeechFunction")) {
                            isReceiving=false;
                            Intent intent = new Intent(MenuActivity.this, SpeechActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (sentence.equalsIgnoreCase("chooseAutomationFunction")) {
                            isReceiving=false;
                            Intent intent = new Intent(MenuActivity.this, Automation.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }


                    } catch (SocketException e) {
e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
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


    @Override
    protected void onStop() {
        super.onStop();
        if (clientSocket != null) {
//            clientSocket.disconnect();
            clientSocket.close();
            clientSocket=null;
        }
        isReceiving = false;
    }
}