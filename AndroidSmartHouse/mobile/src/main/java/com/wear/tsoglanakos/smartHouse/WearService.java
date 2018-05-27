package com.wear.tsoglanakos.smartHouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;

public class WearService extends WearableListenerService {
    private static String UNIQUE_USER_ID;
    //    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        Log.i(WearService.class.getSimpleName(), "WEAR create");
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.i(WearService.class.getSimpleName(), "WEAR destroy");
//    }
//
//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        super.onDataChanged(dataEvents);
//        Log.i(WearService.class.getSimpleName(), "WEAR Data changed ");
//    }
    AutoConnection autoConnection;
    static WearService service;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("onCreate", "onCreate");
        if (UNIQUE_USER_ID == null) {
            generateUniqueUserID();
            Log.e("onCreate", UNIQUE_USER_ID);
//            toast(UNIQUE_USER_ID);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        service = this;

        String message = new String(messageEvent.getData());
        if (client == null) {
            onReadyForContent();
        }

        if (message.equals("AutoSearch")) {

            receiver();
            autoConnection = new AutoConnection(this, false);
            autoConnection.setGoogleClient(client);
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
            return;

        } else if (message.equals("getConnections")) {
            autoConnection = new AutoConnection(this, false);
            autoConnection.setGoogleClient(client);

            sendMessage("/getConnections", autoConnection.getStoredConnetions().getBytes());
        } else if (message.startsWith("Connect to:")) {
            String[] msges = message.split("@@@@@");

            Log.e("sentence", message);
            String ip = msges[0].substring("Connect to:".length(), msges[0].length());

            autoConnection = new AutoConnection(this, false);
            autoConnection.setGoogleClient(client);

            autoConnection.connect(ip, msges[1], msges[2]);
        }


        if (message.equals("speech")) {
            for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
                sendData("chooseSpeechFunction", inetAddress, AutoConnection.port);
            }
        } else if (message.equals("switch")) {
            for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
                sendData("chooseSwitchFunction", inetAddress, AutoConnection.port);
            }
        } else if (message.startsWith("MessageToSend:")) {
            message = message.substring("MessageToSend:".length(), message.length());
            Log.e("MessageToSend", message);

//            if(message.startsWith("getAllOutput")||message.startsWith("getAllCommandsOutput")){
//                if (SwitchManualActivity.switchActivity != null) {
//                    SwitchManualActivity.switchActivity.removeAll();
//                }
//            }
            for (InetAddress inetAddress : AutoConnection.usingInetAddress) {
                sendData2(message, inetAddress, AutoConnection.port);
            }
        }


    }


    private void toast(final String s) {

        Toast.makeText(WearService.this, s, Toast.LENGTH_SHORT).show();

    }


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(this)
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

    private GoogleApiClient client;
    private String    message;
    void sendMessage(final String output, final byte[] payload) {
        if (client == null || !client.isConnected()) {
            onReadyForContent();
        }
        if (client != null) {
            try{
                message= StringUtils.stripAccents(output);
            }catch (Exception e){
                e.printStackTrace();
                message=(output);
            }
            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    List<Node> nodes = getConnectedNodesResult.getNodes();
                    for (Node node : nodes) {


                        Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (sendMessageResult.getStatus().isSuccess()) {
//                                    Toast.makeText(WearService.this, " sended "+new String(payload), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(WearService.this, "not connected with wear device.", Toast.LENGTH_SHORT).show();
                                    client = null;
                                }

                            }
                        });
                    }

                }
            });
        }
    }


    DatagramSocket clientSocket;
    private Thread thread;

    private void receiver() {
        if (thread != null && thread.isAlive()) {
            return;
        }


        thread = new Thread() {
            @Override
            public void run() {
                byte[] receiveData = new byte[10024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            isRunning = false;
                            if (clientSocket != null) {
//                                clientSocket.disconnect();
                                clientSocket.close();
                                clientSocket = null;
                            }

                        }
                    }.execute();


                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();
                    clientSocket.setSoTimeout(2000);
                    clientSocket.receive(receivePacket);
                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    if (sentence.equalsIgnoreCase("chooseSwitchFunction")) {
                        sendMessage("/menu", "switch".getBytes());

                    } else if (sentence.equalsIgnoreCase("chooseSpeechFunction")) {
                        sendMessage("/menu", "speech".getBytes());
                    } else {

                        Log.e("receiver", sentence);
                        sendMessage("/menu", sentence.getBytes());

                        if (SwitchManualActivity.switchActivity != null) {
                            SwitchManualActivity.switchActivity.changeSwitchMode(sentence);
                        }
                    }


                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                thread = null;
            }
        };
        thread.start();
    }

    private boolean isRunning = true;
    private AsyncTask<Void, Void, Void> async;

    private void receiver2() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
//
            if (clientSocket != null) {
//                clientSocket.disconnect();
                clientSocket.close();
                clientSocket = null;
            }
        }
        thread = new Thread() {
            @Override
            public void run() {
                int counter = 0;
                isRunning = true;
                while (isRunning) {
                    byte[] receiveData = new byte[10024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        async = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();

                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (async == this) {
                                    isRunning = false;
                                    if (clientSocket != null) {
//                                        clientSocket.disconnect();
                                        clientSocket.close();
                                        clientSocket = null;

                                    }

                                }
                            }
                        };
                        async.execute();


                        if (clientSocket == null || clientSocket.isClosed())
                            clientSocket = new DatagramSocket();

                        if (counter == 0)
                            clientSocket.setSoTimeout(2500);
                        else {
                            clientSocket.setSoTimeout(1000);
                        }
                        clientSocket.receive(receivePacket);
                        counter++;

                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                        if (sentence.equalsIgnoreCase("chooseSwitchFunction")) {
                            sendMessage("/menu", "switch".getBytes());

                        } else if (sentence.equalsIgnoreCase("chooseSpeechFunction")) {
                            sendMessage("/menu", "speech".getBytes());
                        } else {

                            Log.e("receiver2", sentence);
                            sendMessage("/menu", sentence.getBytes());

                            if (SwitchManualActivity.switchActivity != null) {

                                if (sentence.startsWith("switch ")) {
                                    sentence = sentence.substring("switch ".length(), sentence.length());

                                } else if (sentence.startsWith("switch")) {
                                    sentence = sentence.substring("switch".length(), sentence.length());

                                }
                                SwitchManualActivity.switchActivity.changeSwitchMode(sentence);
                            }
                        }


                    } catch (SocketTimeoutException e) {
                        isRunning = false;
                        if (clientSocket != null) {
//                            clientSocket.disconnect();
                            clientSocket.close();
                            clientSocket = null;

                        }
                        break;
                    } catch (Exception e) {
//                        isRunning = false;
                        if (clientSocket != null) {
//                            clientSocket.disconnect();
                            clientSocket.close();
                            clientSocket = null;

                        }
                        break;
                    }

                }
                thread = null;
            }
        };
        thread.start();
    }

    private void sendData2(final String sendData, final InetAddress IPAddress, final int port) {
        receiver2();
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramPacket sendPacket = new DatagramPacket((UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (UNIQUE_USER_ID + sendData).length(), IPAddress, port);
                    if (clientSocket == null || clientSocket.isClosed())
                        clientSocket = new DatagramSocket();

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public static void generateUniqueUserID() {
        if (UNIQUE_USER_ID == null) {
            UNIQUE_USER_ID = "userUniqueID:" + UUID.randomUUID().toString() + MainActivity.UNIQUE_USER_ID_SPLIT;
        }
    }

    private void sendData(final String output, final InetAddress IPAddress, final int port) {
        receiver();
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
                    DatagramPacket sendPacket = new DatagramPacket((UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (UNIQUE_USER_ID + sendData).length(), IPAddress, port);
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
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
//        service = null;
    }

    //
//    @Override
//    public void onPeerConnected(Node peer) {
//        super.onPeerConnected(peer);
//        //Toast.makeText(WearService.this, "WEAR Connected", Toast.LENGTH_SHORT).show();
//
//        Log.i(WearService.class.getSimpleName(), "WEAR Connected ");
//    }
//
//    @Override
//    public void onPeerDisconnected(Node peer) {
//        super.onPeerDisconnected(peer);
//        Log.i(WearService.class.getSimpleName(), "WEAR Disconnected");
//    }

//
//    private void sendMessage() {
//        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(Bundle bundle) {
//
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//                    }
//                })
//                .build();
//
//        ///
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                mGoogleApiClient.connect();
//            }
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//                for (Node node : nodes.getNodes()) {
//                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/hello", "Hello World".getBytes()).await();
//                    Toast.makeText(WearService.this, result.toString(), Toast.LENGTH_SHORT).show();
//                }
//                return null;
//            }
//\
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                mGoogleApiClient.disconnect();
//            }
//        }.execute();
//
//
//        ///
//    }




}