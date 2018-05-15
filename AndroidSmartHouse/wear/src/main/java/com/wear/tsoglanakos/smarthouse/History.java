package com.wear.tsoglanakos.smarthouse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class History extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {


    private String extra = "connections";
    private WearableListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.history_stab);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.listView);

                String connections = getIntent().getStringExtra(extra);

                String[] list = connections.split("@!@");
                for (String s : list) {

                    listItems.add(s);
                }
                mListView.setAdapter(new MyAdapter(History.this));

            }
        });
    }

    private ArrayList<String> listItems = new ArrayList<String>();

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(client, this);

    }

    private void goToMenu() {
        Intent intent = new Intent(History.this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

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
        }else   if (message.equalsIgnoreCase("wrong")) {
            toast("Wrong username, modify it from mobile device.");
        }
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

    private class MyAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;

        private MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.row_itemlayout, null));
        }


        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            final Button view = (Button) holder.itemView.findViewById(R.id.button);

            String text = listItems.get(position).toString();
            text = text.replace("@@@", " - ");
            view.setText(text);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String [] list=view.getText().toString().split(" - ");

                    sendMessage("/click", ("Connect to:" + list[0] + "@@@@@" + list[1] + "@@@@@" + list[2]).getBytes());


                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            runOnUiThread(new Thread(){
                                @Override
                                public void run() {
                                    if (view != null)
                                        view.setEnabled(false);                               }
                            });

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
                            runOnUiThread(new Thread(){
                                @Override
                                public void run() {
                                    if (view != null)
                                        view.setEnabled(true);                               }
                            });
                            super.onPostExecute(aVoid);
                        }
                    }.execute();

                }
            });
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }


    }

    private GoogleApiClient client;


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(History.this)
                .addConnectionCallbacks(History.this)
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
        if (client == null) {
            onReadyForContent();
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
                            } else {
                                //message sent!
                                toast("not sucess");
                                client = null;
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
                Toast.makeText(History.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
