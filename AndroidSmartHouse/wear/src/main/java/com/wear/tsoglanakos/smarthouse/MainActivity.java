package com.wear.tsoglanakos.smarthouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {
    public final static String WEAR_MESSAGE_PATH = "/message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        WatchViewStub stub = (WatchViewStub) findViewById(R.id.main_activity);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Now you can access your views
                Button conection_button = (Button) stub.findViewById(R.id.connection_button);

                Button direct_connection = (Button) stub.findViewById(R.id.direct_connection);

                direct_connection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage(WEAR_MESSAGE_PATH, "getConnections".getBytes());
                    }
                });
                conection_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // goToMenu();
                        try {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    runOnUiThread(new Thread() {
                                        @Override
                                        public void run() {
                                            v.setEnabled(false);
                                            v.setBackgroundResource(R.drawable.connection_1);
                                            Toast.makeText(MainActivity.this, "Be sure that the mobile phone, is at tha same local network with the server device.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        sendMessage(WEAR_MESSAGE_PATH, "AutoSearch".getBytes());
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


                        } catch (Exception e) {
                            toast("Bind with android phone device first.");
                        }
                    }
                });


            }
        });
    }

    private GoogleApiClient client;


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        toast("Connection Faild");

                    }
                })
                .addApi(Wearable.API)
                .build();

        client.connect();

    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(client!=null){
//        Wearable.MessageApi.removeListener(client, this);
//        client.disconnect();
//            client=null;
//    }
//    }


    private void toast(final String s) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(final String output, final byte[] payload) {
        if (client == null || !client.isConnected()) {

            onReadyForContent();
        }

        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
              String message;
                try{
                    message= StringUtils.stripAccents(output);
                }catch (Exception e){
                    e.printStackTrace();
                    message=(output);
                }

                for (Node node : nodes) {

                    //toast(node.getId());
                    Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {

                            } else {
                                //message sent!
                                toast("not sended");
                                client = null;
                            }
                        }
                    });
                }

            }
        });
    }

    private void goToMenu() {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(client, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (client != null) {
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
            client = null;

        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String message = new String(messageEvent.getData());
        if (message.equalsIgnoreCase("ok")) {
            goToMenu();
            if (client != null) {
                Wearable.MessageApi.removeListener(client, this);
                client.disconnect();
                client = null;

            }
        } else if (messageEvent.getPath().equals("/getConnections")) {
            String extra = "connections";

            if (message.length() > 5) {
                Intent intent = new Intent(MainActivity.this, History.class);
                intent.putExtra(extra, message);
                startActivity(intent);
                if (client != null) {
                    Wearable.MessageApi.removeListener(client, this);
                    client.disconnect();
                    client = null;

                }
            } else {
                toast("You must first add at least one valid IP from mobile device.");
            }
        }
    }
}