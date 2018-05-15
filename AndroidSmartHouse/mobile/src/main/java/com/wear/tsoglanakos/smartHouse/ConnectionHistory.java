package com.wear.tsoglanakos.smartHouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ConnectionHistory extends AppCompatActivity {
    LinearLayout connection_history_linear;
    DB_connectionHistory db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_history);
        connection_history_linear = (LinearLayout) findViewById(R.id.connection_history_linear);
        db = new DB_connectionHistory(this);
        loadHistory();
    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    private void loadHistory() {
        connection_history_linear.setGravity(Gravity.CENTER);
        ArrayList<String> list = db.getAllCotacts();

        for (int i = 0; i < list.size(); i++) {
            RelativeLayout rl = (RelativeLayout) getLayoutInflater().inflate(R.layout.connection_history_tab, null);
            String value = list.get(i);
            final TextView ipText = (TextView) rl.findViewById(R.id.IP_text);
            final TextView portView = (TextView) rl.findViewById(R.id.port_text);
            final TextView usernameText = (TextView) rl.findViewById(R.id.username_text);
            ipText.setText(value.split("@@@")[0]);
            usernameText.setText(value.split("@@@")[1]);
            portView.setText(value.split("@@@")[2]);
            connection_history_linear.addView(rl);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(5));

            final Button connect = (Button) rl.findViewById(R.id.connectButton);
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int usingPort = Integer.parseInt(portView.getText().toString());

                        connect(ipText.getText().toString(), usernameText.getText().toString(),usingPort );
                    }catch (NumberFormatException e){

                        runOnUiThread(new Thread(){
                            @Override
                            public void run() {
                                Toast.makeText(ConnectionHistory.this, "Enter a valid port.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            final Button delete = (Button) rl.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            db.deleteContact(ipText.getText().toString(), usernameText.getText().toString(),portView.getText().toString());
                        }
                    });
                }
            });

            final Button edit = (Button) rl.findViewById(R.id.edit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    editInternetAlgorithm(ipText.getText().toString(), usernameText.getText().toString(),portView.getText().toString());
                }
            });
            View v = new View(this);
            v.setLayoutParams(params);
            connection_history_linear.addView(v);

        }

        Button button = new Button(this);
        button.setGravity(Gravity.CENTER_HORIZONTAL);
        ViewGroup.LayoutParams addNewParams = new ViewGroup.LayoutParams((int) SheduleActivity.pxFromDp(ConnectionHistory.this, 50), (int) SheduleActivity.pxFromDp(ConnectionHistory.this, 50));
        button.setLayoutParams(addNewParams);
        // button.setText("Add new connection");
        button.setBackgroundResource(R.drawable.add2);
        connection_history_linear.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internetAlgorithm();

            }
        });
    }

    public void editInternetAlgorithm(final String inputIp, final String inputUsername, final String inputPort) {

        try {


            final EditText ipInput = new EditText(ConnectionHistory.this);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());
            ip = ipInput.getText().toString();
            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());
            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Enter ip");
            txtView1.setGravity(Gravity.CENTER_VERTICAL);
            txtView1.setTextColor(Color.BLACK);
            ll.addView(txtView1, new LinearLayout.LayoutParams(dpToPx(100), ViewGroup.LayoutParams.WRAP_CONTENT));
            ipInput.setGravity(Gravity.CENTER);
            ll.addView(ipInput);
            ipInput.setMinEms(10);

            final EditText usernameInput = new EditText(ConnectionHistory.this);


//            usernameInput.setText(settings.getString("NickName", "").toString());
            usernameInput.setFocusable(true);
            usernameInput.setClickable(true);
            usernameInput.setFocusableInTouchMode(true);
            usernameInput.setSelectAllOnFocus(true);
            usernameInput.setSingleLine(true);
            usernameInput.setText(inputUsername);
            ipInput.setText(inputIp);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            TextView txtView2 = new TextView(getApplicationContext());
            txtView2.setText("Computer's Name");
            txtView2.setGravity(Gravity.CENTER_VERTICAL);
            txtView2.setTextColor(Color.BLACK);
            usernameInput.setMinEms(10);
            ll2.addView(txtView2);
            usernameInput.setGravity(Gravity.CENTER);
            ll2.addView(usernameInput);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 50));
            showLayout.addView(ll);

            TextView portText = new TextView(getApplicationContext());
            portText.setText("Enter port");
            portText.setGravity(Gravity.CENTER_VERTICAL);
            portText.setTextColor(Color.BLACK);
            final EditText portInput = new EditText(ConnectionHistory.this);
            portInput.setMinEms(10);
            portInput.setText(inputPort);
            LinearLayout ll3 = new LinearLayout(getApplicationContext());
            portInput.setGravity(Gravity.CENTER);
            ll3.setOrientation(LinearLayout.HORIZONTAL);
            ll3.addView(portText, new LinearLayout.LayoutParams(dpToPx(100), ViewGroup.LayoutParams.WRAP_CONTENT));
            ll3.addView(portInput);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());

            showLayout.addView(ll3);

            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 200));
            showLayout.addView(ll2);




            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Be sure you have the same username on computer", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {

                            }
                        }
                    });
                    new AlertDialog.Builder(ConnectionHistory.this)
                            .setTitle("Remote Controll with public IP")
                            //.setMessage("Enter Public ip")

                            .setView(showLayout)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ip = ipInput.getText().toString();
                                    if (ip == null || ip.replace(" ", "").equals("") || !validate(ip)) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real IP", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    String usernameEntered = usernameInput.getText().toString();
                                    if (usernameEntered == null || usernameEntered.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real Username", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    if (ip == null || ip.equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Not Valid IP .. ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }

                                    ///////////////////////////////
//                                    connect(usernameInput.getText().toString());
                                    if (ipInput != null && !ipInput.getText().toString().replaceAll("", "").equals("") && usernameInput != null && !usernameInput.getText().toString().replaceAll("", "").equals("")) {

                                        if (!db.isStored(ipInput.getText().toString(), usernameInput.getText().toString(),portInput.getText().toString())) {
                                            runOnUiThread(new Thread() {
                                                @Override
                                                public void run() {
                                                    db.deleteContactAndCreateAnotherOne(inputIp, inputUsername, ipInput.getText().toString(), usernameInput.getText().toString(),inputPort,portInput.getText().toString());
                                                }
                                            });

                                        } else {
                                            runOnUiThread(new Thread() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ConnectionHistory.this, "is Already stored", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ConnectionHistory.this, "Enter real values", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                            .show();
                }
            });


        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Error:  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    String ip = null;

    public void internetAlgorithm() {

        try {


            final EditText ipInput = new EditText(ConnectionHistory.this);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());
            ip = ipInput.getText().toString();
            LinearLayout ll = new LinearLayout(getApplicationContext());
            final LinearLayout showLayout = new LinearLayout(getApplicationContext());
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText("Save data.");
            checkBox.setChecked(true);
            showLayout.setOrientation(LinearLayout.VERTICAL);
            TextView txtView1 = new TextView(getApplicationContext());
            txtView1.setText("Enter ip");
            txtView1.setGravity(Gravity.CENTER_VERTICAL);
            txtView1.setTextColor(Color.BLACK);
            ll.addView(txtView1, new LinearLayout.LayoutParams(dpToPx(100), ViewGroup.LayoutParams.WRAP_CONTENT));
            ll.addView(ipInput);
            ipInput.setMinEms(10);
            ipInput.setGravity(Gravity.CENTER);
            final EditText usernameInput = new EditText(ConnectionHistory.this);
            usernameInput.setText("home");
//            usernameInput.setText(settings.getString("NickName", "").toString());
            usernameInput.setFocusable(true);
            usernameInput.setClickable(true);
            usernameInput.setFocusableInTouchMode(true);
            usernameInput.setSelectAllOnFocus(true);
            usernameInput.setSingleLine(true);
            usernameInput.setGravity(Gravity.CENTER);
            final LinearLayout ll2 = new LinearLayout(getApplicationContext());
            TextView txtView2 = new TextView(getApplicationContext());
            txtView2.setText("Device Name");
            txtView2.setGravity(Gravity.CENTER_VERTICAL);
            txtView2.setTextColor(Color.BLACK);
            usernameInput.setMinEms(10);
            ll2.addView(txtView2, new LinearLayout.LayoutParams(dpToPx(100), ViewGroup.LayoutParams.WRAP_CONTENT));
            ll2.addView(usernameInput);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 50));
            showLayout.addView(ll);



            TextView portText = new TextView(getApplicationContext());
            portText.setText("Enter port");
            portText.setGravity(Gravity.CENTER_VERTICAL);
            portText.setTextColor(Color.BLACK);
            final EditText portInput = new EditText(ConnectionHistory.this);
            portInput.setMinEms(10);
            portInput.setText(Integer.toString(AutoConnection.port));
            LinearLayout ll3 = new LinearLayout(getApplicationContext());
            portInput.setGravity(Gravity.CENTER);
            ll3.setOrientation(LinearLayout.HORIZONTAL);
            ll3.addView(portText, new LinearLayout.LayoutParams(dpToPx(100), ViewGroup.LayoutParams.WRAP_CONTENT));
            ll3.addView(portInput);
//            SharedPreferences settings = getSharedPreferences("RemoteControl", 0);
//            ipInput.setText(settings.getString("Global Ip", "").toString());

            showLayout.addView(ll3);
            showLayout.addView(new View(getApplicationContext()), new ViewGroup.LayoutParams(100, 200));
            showLayout.addView(ll2);
            showLayout.addView(checkBox);
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getApplicationContext(), "Be sure you have the same name on device", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {

                            }
                        }
                    });
                    new AlertDialog.Builder(ConnectionHistory.this)
                            .setTitle("Smart house global connection")
                            //.setMessage("Enter Public ip")

                            .setView(showLayout)

                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ip = ipInput.getText().toString();
                                    if (ip == null || ip.replace(" ", "").equals("") || !validate(ip)) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real IP", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    String usernameEntered = usernameInput.getText().toString();
                                    if (usernameEntered == null || usernameEntered.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real device name", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                        return;
                                    }
                                    String portEntered = portInput.getText().toString();
                                    int usingPort = -1;
                                    try {
                                        usingPort = Integer.parseInt(portEntered);
                                    } catch (Exception e) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a valid port, only numbers accepted.", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                    if (portEntered == null || portEntered.replace(" ", "").equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {

                                                Toast.makeText(getApplicationContext(), "Enter a real port", Toast.LENGTH_SHORT).show();

                                            }
                                        });


                                        return;
                                    }
//                                    if (ip != ip2) {
////                                        ddPreferences settings = getSharedPreferences("RemoteControl", 0);
////                                        SharedPreferences.Editor editor = settings.edit();
////                                        editor.putString("Global Ip", ipInput.getText().toString());
////
////
////                                        editor.putString("NickName", usernameInput.getText().toString());
////
////
////                                        editor.commit();
//                                        // deal with the editable
//                                    }
                                    if (ip == null || ip.equals("")) {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Not Valid IP .. ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }

                                    ///////////////////////////////
//                                    connect(usernameInput.getText().toString());
                                    if (ipInput != null && !ipInput.getText().toString().replaceAll("", "").equals("") && usernameInput != null && !usernameInput.getText().toString().replaceAll("", "").equals("")) {
                                        if (checkBox.isChecked()) {
                                            if (!db.isStored(ipInput.getText().toString(), usernameInput.getText().toString(), portInput.getText().toString())) {
                                                db.insertContact(ipInput.getText().toString(), usernameInput.getText().toString(), portInput.getText().toString());
                                                startActivity(new Intent(ConnectionHistory.this, ConnectionHistory.class));
                                            } else {
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ConnectionHistory.this, "is Already stored", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            if (usingPort > 0) {
                                                connect(ipInput.getText().toString(), usernameEntered, usingPort);
                                            } else {
                                                runOnUiThread(new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "Port must be a number bigger than 0. ", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        runOnUiThread(new Thread() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ConnectionHistory.this, "Enter real values", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }

                    )
                            .

                                    show();
                }
            });

//                        s.connect((new InetSocketAddress(InetAddress.getByName("78.87.53.120"), 2000)), 5000);

//                    s = new Socket(ip, 6667);


        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Error:  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    public void connect(final String ip, String name,int port) {
        try {
            AutoConnection.port=port;
            sendData("globalReturning", name.replaceAll("!!!!!", ""), InetAddress.getByName(ip), AutoConnection.port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private DatagramSocket clientSocket;

    private void sendData(final String output, final String name, final InetAddress IPAddress, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final  String    sendData= StringUtils.stripAccents(output);

                    DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID + sendData + name).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID + sendData + name).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();
                    AutoConnection.usingInetAddress.add(IPAddress);
                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private void receiver() {
        new Thread() {
            @Override
            public void run() {

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    if (clientSocket == null)
                        clientSocket = new DatagramSocket();
                    clientSocket.receive(receivePacket);
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    Log.e("sentence", sentence);
                    if (sentence.equals("wrong")) {
                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {
                                Toast.makeText(ConnectionHistory.this, "Wrong username. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        receiver();
                    } else {
                        startActivity(new Intent(ConnectionHistory.this, MenuActivity.class));
                    }
                } catch (SocketException e) {

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (clientSocket != null) {
//            clientSocket.disconnect();
//            clientSocket.close();
            clientSocket=null;
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


//    public int pxToDp(int px) {
//        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
//        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        return dp;
//    }


    @Override
    public void onBackPressed() {
Intent intent=new Intent(ConnectionHistory.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


}