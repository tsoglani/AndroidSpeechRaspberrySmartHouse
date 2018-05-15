package com.wear.tsoglanakos.smartHouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by tsoglani on 18/1/2016.
 */
public class AutoConnection {

    static int port = 2222;
    private DatagramSocket clientSocket;
    private Context context;
    boolean isFromPhone;
    private GoogleApiClient client;

    static ArrayList<InetAddress> usingInetAddress = new ArrayList<InetAddress>() {
        @Override
        public boolean add(InetAddress object) {
            if (!contains(object))
                return super.add(object);
            return false;
        }
    };

    public AutoConnection(Context context, boolean isFromPhone) {
        this.context = context;
        this.isFromPhone = isFromPhone;
    }

    public void setGoogleClient(GoogleApiClient client){
        this.client=client;
    }
    public void sendToAllIpInNetwork() throws UnknownHostException, IOException {
        ArrayList<String> ipList = getLocal();
        clientSocket = new DatagramSocket();
        receiver2();
        for (final String ip : ipList) {
            if (ip.replaceAll(" ", "").equals("")) {
                continue;
            }
            try {
                for (int i = 1; i < 255; i++) {
                    final String checkIp = ip + i;
                    // Log.e("ip=",checkIp);
                    String sendData = MainActivity.UNIQUE_USER_ID+"returning";
                    InetAddress IPAddress = InetAddress.getByName(checkIp);
                    DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.length(), IPAddress, port);
                    clientSocket.send(sendPacket);

                }
//                if (!isFromPhone) {
//                   sendToStoredIPs();
//                }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String getStoredConnetions() {
        DB_connectionHistory db = new DB_connectionHistory(context);
        final ArrayList<String> list = db.getAllCotacts();


        String output = "";
        for (int i = 0; i < list.size(); i++) {
            if (i != list.size() - 1)
                output += list.get(i) + "@!@";
            else {
                output += list.get(i);

            }
        }
        return output;
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
                    AutoConnection.usingInetAddress.add(IPAddress);

                    clientSocket.send(sendPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }




//    public void connect(final String ip,String userName) {
//        try {
//            receiver();
//            new Thread(){
//                @Override
//                public void run() {
//                    super.run();
//                }
//            }.start();
//            sendData("globalReturning" + userName.replaceAll("!!!!!", ""), InetAddress.getByName(ip), AutoConnection.port);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }

    public void connect(final String ip,String userName,String port) {
        try {
            receiver();
            try {
                int usingPort = Integer.parseInt(port);
                if(MainActivity.UNIQUE_USER_ID==null){
                    MainActivity.generateUniqueUserID();
                }
                AutoConnection.port=usingPort;
                sendData("globalReturning" + userName.replaceAll("!!!!!", ""), InetAddress.getByName(ip), AutoConnection.port);
            }catch (NumberFormatException e ){
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendToStoredIPs() {
        DB_connectionHistory db = new DB_connectionHistory(context);
        final ArrayList<String> list = db.getAllCotacts();


        for (int i = 0; i < list.size(); i++) {
            try {
                String value = list.get(i);
                String ipText = value.split("@@@")[0];
                String sendData = MainActivity.UNIQUE_USER_ID+"returning";
                InetAddress IPAddress = InetAddress.getByName(ipText);
                DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.length(), IPAddress, port);
                clientSocket.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void receiver() {
        new Thread() {
            @Override
            public void run() {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    if(clientSocket==null)
                        clientSocket= new DatagramSocket();
//                    Toast.makeText(context, "waiting", Toast.LENGTH_SHORT).show();
                    clientSocket.setSoTimeout(5000);
                    clientSocket.receive(receivePacket);
                    String modifiedSentence = new String(receivePacket.getData()); // 1rst connection respond
                    if (!usingInetAddress.contains(receivePacket.getAddress()))
                        usingInetAddress.add(receivePacket.getAddress());

                    String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                    if (isFromPhone) {
                        Intent intent = new Intent(context, MenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    } else {
                        Log.e("sentence",sentence);

                        if(sentence.equals("wrong")){
                            sendMessage("/AutoSearch", "wrong".getBytes());

                        }else sendMessage("/AutoSearch", "ok".getBytes());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private void receiver2() {
        new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
//                    Toast.makeText(context, "waiting", Toast.LENGTH_SHORT).show();
                        clientSocket.setSoTimeout(6000);
                        clientSocket.receive(receivePacket);
                        String modifiedSentence = new String(receivePacket.getData()); // 1rst connection respond
                        if (!usingInetAddress.contains(receivePacket.getAddress()))
                            usingInetAddress.add(receivePacket.getAddress());

                        String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                        if (isFromPhone) {
                            Intent intent = new Intent(context, MenuActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent);
                        } else {
                            Log.e("sentence", sentence);

                            if (sentence.equals("wrong")) {
                                sendMessage("/AutoSearch", "wrong".getBytes());

                            } else sendMessage("/AutoSearch", "ok".getBytes());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private void toast(final String msg) {
        ((Activity) context).runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText((context), msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private static ArrayList<String> getLocal() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        ArrayList<String> list = new ArrayList<String>();
        while (e.hasMoreElements()) {

            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {

                InetAddress inet = (InetAddress) ee.nextElement();
                if (!inet.isLinkLocalAddress()) {
                    String hostAdd = inet.getHostAddress();
                    String str = "";
                    String[] ars = hostAdd.split("\\.");
                    for (int j = 0; j < ars.length - 1; j++) {
                        str += ars[j] + ".";
                    }
                    list.add(str);
                }
            }
        }
        return list;
    }

    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(context)
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


    private void sendMessage(final String message, final byte[] payload) {
        if(client==null||!client.isConnected())
            onReadyForContent();
        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {


                    Wearable.MessageApi.sendMessage(client, node.getId(), message, payload).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {

                            }else{
                                client=null;
                            }

                        }
                    });
                }

            }
        });
    }
}