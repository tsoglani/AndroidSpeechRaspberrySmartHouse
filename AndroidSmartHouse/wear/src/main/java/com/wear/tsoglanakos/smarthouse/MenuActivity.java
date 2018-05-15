package com.wear.tsoglanakos.smarthouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
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

public class MenuActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // Now you can access your views

                Button speechButton = (Button) stub.findViewById(R.id.speechButton);
                speechButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        onReadyForContent();
                        new Thread() {
                            @Override
                            public void run() {

                                sendMessage(MainActivity.WEAR_MESSAGE_PATH, "speech".getBytes());

                            }
                        }.start();
                    }
                });
                Button switch_button = (Button) stub.findViewById(R.id.switch_button);
                switch_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        onReadyForContent();
                        new Thread() {
                            @Override
                            public void run() {
                                sendMessage(MainActivity.WEAR_MESSAGE_PATH, "switch".getBytes());

                            }
                        }.start();

                    }
                });
            }
        });


    }


    public void goToSwiftActivity(View view) {
        Intent intent = new Intent(this, SwiftActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(client!=null){
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
            client=null;

        }
    }

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);
//            final TextView textView = (TextView) findViewById(R.id.textView);


            new Thread() {
                @Override
                public void run() {
                    sendMessage(MainActivity.WEAR_MESSAGE_PATH, ("MessageToSend:" + spokenText).getBytes());
                }
            }.start();
            toast(spokenText + " command sended");
//            textView.setText(spokenText);
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);

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


    private void sendMessage(final String output, final byte[] payload) {
        if (client == null || !client.isConnected()) {
            onReadyForContent();
        }
        if (client != null)
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

                        Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (sendMessageResult.getStatus().isSuccess()) {
                                } else {
                                    //message sent!
                                    toast("not sucess");
                                }

                            }
                        });
                    }

                }
            });
    }

    private void toast(final String s) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(MenuActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(client, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(MenuActivity.class.getSimpleName(), "Connection failed");
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String message = new String(messageEvent.getData());
        if (message.equals("speech")) {
            displaySpeechRecognizer();
            if (client != null) {
                Wearable.MessageApi.removeListener(client, this);
                client.disconnect();
            }
            client = null;
        } else if (message.equals("switch")) {
            goToSwiftActivity(null);
            if (client != null) {
                Wearable.MessageApi.removeListener(client, this);
                client.disconnect();
            }
            client = null;
        }


    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Wearable.MessageApi.removeListener(client, this);
//        client.disconnect();
//    }


}