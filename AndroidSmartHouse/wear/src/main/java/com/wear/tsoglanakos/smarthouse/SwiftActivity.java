package com.wear.tsoglanakos.smarthouse;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwiftActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private WearableListView mListView;
    private boolean isCommandMode;
    private final String commandModeID = "isCommandMode_wearable";
    private final String MY_PREFS_NAME = "Wearable_Smart_House";
    // boolean[] checkBoxState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swift);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {


                mListView = (WearableListView) stub.findViewById(R.id.listView1);
                isCommandMode = receiveBoolean(commandModeID, true);
                listItems.removeAll(listItems);
                mListView.removeAllViews();
//                if (!isCommandMode)
//                 toast(Boolean.toString(isCommandMode));
                selectMode();
                Switch sb = (Switch) stub.findViewById(R.id.switch2);
                sb.setChecked(isCommandMode);
                sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        storeBoolean(commandModeID, isChecked);
                        isCommandMode = isChecked;
                        mListView.scrollToPosition(0);

                        listItems.removeAll(listItems);

                        mListView.removeAllViews();

//                        mListView.removeAllViews();
//                        mListView.removeAllViews();
                        selectMode();


                    }
                });

                final Button refresh_button = (Button) stub.findViewById(R.id.refresh);
                refresh_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        runOnUiThread(new Thread() {
                            @Override
                            public void run() {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected void onPreExecute() {
                                        mListView.scrollToPosition(0);

                                        listItems.removeAll(listItems);

                                        mListView.removeAllViews();
//
//                                                selectMode();
                                        refresh_button.setEnabled(false);
//                                        mListView.removeAllViews();
                                    }

                                    @Override
                                    protected Void doInBackground(Void... params) {

                                        selectMode();
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        refresh_button.setEnabled(true);
                                        refresh_button.setEnabled(true);
                                    }
                                }.execute();
                            }
                        });

                    }
                });
            }
        });


    }


    private void storeBoolean(String dataID, boolean data) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(dataID, data);
        editor.commit();
    }

    private boolean receiveBoolean(String dataID, boolean defaultReturningBoolean) {

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean(dataID, defaultReturningBoolean);
        return restoredText;

    }

    private static ArrayList<String> listItems = new ArrayList<String>();
//    static {
//        listItems;
//        listItems.add("Monday");
//        listItems.add("Tuesday");
//        listItems.add("Wednesday");
//        listItems.add("Thursday");
//        listItems.add("Friday");
//        listItems.add("Saturday");
//    }


    private void selectMode() {


        new Thread() {
            @Override
            public void run() {
                if (isCommandMode) {
                    sendMessage("/mode", ("MessageToSend:" + "getAllCommandsOutput").getBytes());
                } else {
                    sendMessage("/mode", ("MessageToSend:" + "getAllOutput").getBytes());
                }
            }
        }.start();

    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(client, this);

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String message = new String(messageEvent.getData());

//        Log.e(messageEvent.getPath(), message);
        if (messageEvent.getPath().equals("/switch")) {
            changeSwitchMode(message);
        } else

            receiveStringProcess(message);

    }

    private void receiveStringProcess(String input) {

//    final    String in2=input;
//runOnUiThread(new Thread() {
//    @Override
//    public void run() {
//        Toast.makeText(SwitchManualActivity.this, in2, Toast.LENGTH_SHORT).show();
//    }
//});
        Log.e("input", input);

        if (input.startsWith("respondGetAllOutput")) {
//            listItems.removeAll(listItems);
//            mListView.removeAllViews();
            input = input.substring("respondGetAllOutput".length(), input.length());
            final String[] pinax = input.split("@@@");
            if (pinax.length > 1) {
                changeSwitchMode(pinax);
            } else {
                changeSwitchMode(input);
            }

        } else if (input.startsWith("respondGetAllCommandsOutput")) {
//            listItems.removeAll(listItems);
//            mListView.removeAllViews();
            input = input.substring("respondGetAllCommandsOutput".length(), input.length());
            final String[] pinax = input.split("@@@");

            Log.e("", input);
            if (pinax.length > 1) {
                changeSwitchMode(pinax);
            } else {

                changeSwitchMode(input);
            }
        } else {
            if (input.startsWith("switch ")) {
                input = input.substring("switch ".length(), input.length());
            } else if (input.startsWith("switch")) {
                input = input.substring("switch".length(), input.length());
            }
            changeSwitchMode(input);

        }


    }


    private void loadAutoMode(ArrayList<String> list) {

//        mListView.removeAllViews();
        mListView.setAdapter(new MyAdapter(SwiftActivity.this));


        for (int i = 0; i < list.size(); i++) {
            FrameLayout rl = (FrameLayout) getLayoutInflater().inflate(R.layout.row_simple_item_layout, null);
            final String value = list.get(i);

            final CheckBox switchButton = (CheckBox) rl.findViewById(R.id.switch1);

            String textForEachSell = value;

            if (textForEachSell.substring(textForEachSell.length() - 2, textForEachSell.length()).equalsIgnoreCase("on")) {
                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 3);
//                switchButton.setChecked(true);
//                Log.e("On",list.get(i));

            } else if (textForEachSell.substring(textForEachSell.length() - 3, textForEachSell.length()).equalsIgnoreCase("off")) {
                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 4);
//                switchButton.setChecked(false);
            }
            changeSwitchMode(textForEachSell);
            switchButton.setText(textForEachSell);


        }
    }

    private void changeSwitchMode(final ArrayList<String> list) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {

                loadAutoMode(list);
                for (int i = 0; i < list.size(); i++) {
                    changeSwitchMode(list.get(i));
                }
            }
        });

    }

    private void changeSwitchMode(final String[] list) {
        ArrayList arrayList = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            String textForEachSell = list[i];
            if (textForEachSell.startsWith("unknown")) {
                continue;
            }

            arrayList.add(textForEachSell);

//
//            if (textForEachSell.substring(textForEachSell.length() - 2, textForEachSell.length()).equalsIgnoreCase("on")) {
//                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 3);
//            } else if (textForEachSell.substring(textForEachSell.length() - 3, textForEachSell.length()).equalsIgnoreCase("off")) {
//                textForEachSell = textForEachSell.substring(0, textForEachSell.length() - 4);
//            }

            listItems.add(textForEachSell);
        }
        changeSwitchMode(arrayList);
    }

    private void changeSwitchMode(String input) {


        if (input.length() > 2 && input.substring(input.length() - 2, input.length()).equalsIgnoreCase("on") || input.length() > 3 && input.substring(input.length() - 3, input.length()).equalsIgnoreCase("off")) {

            String cutString = null;
            boolean state = false;
            if (input.substring(input.length() - 2, input.length()).equalsIgnoreCase("on")) {
                cutString = input.substring(0, input.length() - 3);
                state = true;

            }
            if (input.length() > 4 && input.substring(input.length() - 3, input.length()).equalsIgnoreCase("off")) {
                cutString = input.substring(0, input.length() - 4);
                state = false;
            }
            if (cutString == null) {
                return;
            }
            final boolean finalState = state;


            for (int i = 0; i < listItems.size(); i++) {
                String text = listItems.get(i).toString();
                if (text.endsWith(" on")) {
                    text = text.substring(0, text.length() - " on".length());
                } else if (text.endsWith(" off")) {
                    text = text.substring(0, text.length() - " off".length());

                }
                Log.e("cutString= " + cutString, "text= " + text);
                if (cutString.equals(text)) {
                    Log.e("cutString= text", "equals ");
                    listItems.remove(i);
                    if (state) {
                        text += " on";
                    } else {

                        text += " off";
                    }
                    listItems.add(i, text);
                }
            }

            for (int i = 0; i < mListView.getChildCount(); i++) {

                final int finalI = i;
                View v = mListView.getChildAt(i);
                if (v instanceof FrameLayout) {
                    FrameLayout rl = (FrameLayout) v;
                    for (int j = 0; j < rl.getChildCount(); j++) {
                        View rlChild = rl.getChildAt(j);

                        if (rlChild instanceof CheckBox) {
                            final CheckBox aSwitch = (CheckBox) rlChild;
                            if ((aSwitch.getText().toString()).equalsIgnoreCase(cutString)) {


                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        aSwitch.setChecked(finalState);

//                                        String text = listItems.get(finalI).toString();
//                                        String mode;
//                                        if (finalState) {
//                                            mode = " on";
//                                        } else {
//                                            mode = " off";
//                                        }
//                                        if (text.endsWith(" on")) {
//                                            text = listItems.get(finalI).toString().substring(0, listItems.get(finalI).toString().length() - " on".length());
//                                            text += mode;
//                                            listItems.remove(finalI);
//                                            listItems.add(finalI, text);
//                                        } else if (text.endsWith(" off")) {
//                                            text = listItems.get(finalI).toString().substring(0, listItems.get(finalI).toString().length() - " off".length());
//                                            text += mode;
//                                            listItems.remove(finalI);
//                                            listItems.add(finalI, text);
//                                        }

                                        //   checkBoxState[finalI] = finalState;
                                    }
                                });

                            } else if ((aSwitch.getText().toString()).startsWith(cutString)) {

                                final String parseToBool = aSwitch.getText().toString().substring(cutString.length(), (aSwitch.getText().toString()).length()).replaceAll(" ", "");
                                runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        aSwitch.setChecked(Boolean.parseBoolean(parseToBool));
//                                        String text = listItems.get(finalI).toString();
//                                        String mode;
//                                        if (Boolean.parseBoolean(parseToBool)) {
//                                            mode = " on";
//                                        } else {
//                                            mode = " off";
//                                        }
//                                        if (text.endsWith(" on")) {
//                                            text = listItems.get(finalI).toString().substring(0, listItems.get(finalI).toString().length() - " on".length());
//                                            text += mode;
//                                            listItems.remove(finalI);
//                                            listItems.add(finalI, text);
//                                        } else if (text.endsWith(" off")) {
//                                            text = listItems.get(finalI).toString().substring(0, listItems.get(finalI).toString().length() - " off".length());
//                                            text += mode;
//                                            listItems.remove(finalI);
//                                            listItems.add(finalI, text);
//                                        }
                                        //  checkBoxState[finalI] = aSwitch.isChecked();
                                    }
                                });

                            }
                        }
                    }
                }
            }
        }
    }


    private class MyAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;
        private WearableListView.ViewHolder holder;

        private MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            //  checkBoxState = new boolean[listItems.size()];
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.row_simple_item_layout, null));
        }


        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {
            final CheckBox view = (CheckBox) holder.itemView.findViewById(R.id.switch1);
            this.holder = holder;
            String text = listItems.get(position).toString();
            if (text.endsWith(" on")) {
                view.setChecked(true);
                //   checkBoxState[position] = true;
                if (text.length() > 3)
                    text = text.substring(0, text.length() - 3);
                else if (text.length() > 2)
                    text = text.substring(0, text.length() - 2);
            } else if (text.endsWith(" off")) {
                view.setChecked(false);
                //   checkBoxState[position] = false;
                if (text.length() > 4)
                    text = text.substring(0, text.length() - 4);
                else if (text.length() > 3)
                    text = text.substring(0, text.length() - 3);
            }


//            view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                    String text = listItems.get(position).toString();
////                    String mode;
////                    if(isChecked){
////                        mode=" on";
////                    }else{
////                        mode =" off";
////                    }
////                    if (text.endsWith(" on")) {
////                        text=  listItems.get(position).toString().substring(0,listItems.get(position).toString().length()-" on".length());
////                        text+=mode;
////                        listItems.remove(position);
////                        listItems.add(position,text);
////                    } else if (text.endsWith(" off")) {
////                        text=  listItems.get(position).toString().substring(0,listItems.get(position).toString().length()-" off".length());
////                        text+=mode;
////                        listItems.remove(position);
////                        listItems.add(position,text);
////                    }
//                }
//            });

//            view.setChecked(checkBoxState[position]);
//toast("onBindViewHolder");
            view.setText(text);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String extra = null;

                    view.setChecked(!view.isChecked());
                    if (!view.isChecked()) {
                        extra = " on";

                    } else {
                        extra = " off";
                    }
//                    checkBoxState[position] = view.isChecked();
                    sendMessage("/click", ("MessageToSend:switch " + view.getText().toString() + extra).getBytes());
                }
            });

            holder.itemView.setTag(position);
        }

        @Override
        public long getItemId(int position) {


            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {


            return listItems.size();
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            Wearable.MessageApi.removeListener(client, this);
            client.disconnect();
            client = null;

        }
    }

    private GoogleApiClient client;


    public void onReadyForContent() {
        client = new GoogleApiClient.Builder(SwiftActivity.this)
                .addConnectionCallbacks(SwiftActivity.this)
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
                Toast.makeText(SwiftActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (client != null) {
//            Wearable.MessageApi.removeListener(client, this);
//            client.disconnect();
//            client = null;
//
//        }
//    }
}