package com.wear.tsoglanakos.smartHouse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import org.apache.commons.lang3.StringUtils;

public class SpeechActivity extends AppCompatActivity {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static final String LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchToSpeech();
    }

    private void switchToKeyboard() {

        setContentView(R.layout.keyboard_view);
        final EditText txtTextInput = (EditText) findViewById(R.id.txtTextInput);

        txtTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    sendTextOnSwitchText(null);
                    hideSoftKeyboard();
                }
                return true;
            }
        });
//        txtTextInput.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
//
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                // TODO Auto-generated method stub
//                return false;
//            }
//
//            public void onDestroyActionMode(ActionMode mode) {
//                // TODO Auto-generated method stub
//
//            }
//
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                // TODO Auto-generated method stub
//                return false;
//            }
//
//            public boolean onActionItemClicked(ActionMode mode,
//                                               MenuItem item) {
//                // TODO Auto-generated method stub
//                return false;
//            }
//        });
//        txtTextInput.setLongClickable(false);
//        txtTextInput.setTextIsSelectable(false);

    }

    public void hideSoftKeyboard() {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(
                                Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null && getCurrentFocus() != null)
                    inputMethodManager.hideSoftInputFromWindow(
                            getCurrentFocus().getWindowToken(), 0);

            }
        });

    }

    private void switchToSpeech() {
        hideSoftKeyboard();
        setContentView(R.layout.activity_speech);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);


        // hide the action bar
//        getActionBar().hide();
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isOnline())
                    promptSpeechInput();
                else
                    toast("No internet connection.");
            }
        });

        String language = getValue(this, LANGUAGE, "Eng");


        RadioGroup rg = (RadioGroup) findViewById(R.id.radioSex);
        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        RadioButton radioEng = (RadioButton) findViewById(R.id.radioEng);

        if (language.equals("Auto")) {
            radioAuto.setChecked(true);
            radioEng.setChecked(false);
        } else {
            radioAuto.setChecked(false);
            radioEng.setChecked(true);
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
                if (radioAuto.getId() == checkedId) {
                    save(SpeechActivity.this, LANGUAGE, "Auto");
                } else {
                    save(SpeechActivity.this, LANGUAGE, "Eng");

                }
            }
        });

    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                if(txtSpeechInput!=null)
                    txtSpeechInput.setText("");
            }
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        RadioButton radioAuto = (RadioButton) findViewById(R.id.radioAuto);
        if (radioAuto.isChecked()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        } else {
            intent.putExtra("en-US",
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        }
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            toast(getString(R.string.speech_not_supported));
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    final ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));


                }
                break;
            }

        }
    }
//
//    static String greekToGreeklish(String input) {
//        String output = new String();
//        for (int i = 0; i < input.length(); i++) {
//            output += getGreeklishChar(input.charAt(i));
//        }
//        return output;
//    }

    private static String getGreeklishChar(char greekChar) {
        String greeklishString = null;
        switch (greekChar) {
            case 'α':
            case 'ά':
                greeklishString = "a";
                break;
            case 'β':
                greeklishString = "v";
                break;
            case 'γ':
                greeklishString = "g";
                break;
            case 'δ':
                greeklishString = "d";
                break;
            case 'ε':
            case 'έ':
                greeklishString = "e";
                break;
            case 'ζ':
                greeklishString = "z";
                break;
            case 'η':
            case 'ή':
            case 'ι':
            case 'ί':
                greeklishString = "i";
                break;
            case 'θ':
                greeklishString = "th";
                break;
            case 'κ':
                greeklishString = "k";
                break;
            case 'λ':
                greeklishString = "l";
                break;
            case 'μ':
                greeklishString = "m";
                break;
            case 'ν':
                greeklishString = "n";
                break;
            case 'ξ':
                greeklishString = "ks";
                break;
            case 'ό':
            case 'ο':
            case 'ω':
            case 'ώ':
                greeklishString = "o";
                break;
            case 'π':
                greeklishString = "p";
                break;
            case 'ρ':
                greeklishString = "r";
                break;
            case 'σ':
            case 'ς':
                greeklishString = "s";
                break;
            case 'τ':
                greeklishString = "t";
                break;
            case 'υ':
            case 'ύ':
                greeklishString = "y";
                break;
            case 'φ':
                greeklishString = "f";
                break;
            case 'χ':
                greeklishString = "x";
                break;
            case 'ψ':
                greeklishString = "ps";
                break;
            default:
                greeklishString = Character.toString(greekChar);
        }

        return greeklishString;
    }

    DatagramSocket clientSocket;

    private void sendData(String output, InetAddress IPAddress, int port) throws IOException {

        String sendData = output;
        try {
            sendData = StringUtils.stripAccents(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DatagramPacket sendPacket = new DatagramPacket((MainActivity.UNIQUE_USER_ID + sendData).getBytes("UTF-8"), (MainActivity.UNIQUE_USER_ID + sendData).length(), IPAddress, port);
//        toast(sendData);
        if (clientSocket == null || clientSocket.isClosed())
            clientSocket = new DatagramSocket();


        clientSocket.send(sendPacket);
    }

    private void toast(final String msg) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                Toast.makeText(SpeechActivity.this, msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void goBack(View v) {
        onBackPressed();
    }


    public static void save(Activity act, String valueCodeName, String value) {
        SharedPreferences.Editor editor = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(valueCodeName, value);
        editor.commit();
    }


    public static String getValue(Activity act, String valueCodeName, String defaultValue) {
        SharedPreferences prefs = act.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString(valueCodeName, defaultValue);

        return restoredText;
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientSocket != null) {
//            clientSocket.disconnect();
            clientSocket.close();
            clientSocket = null;
        }
        isReceiving = false;

    }

    Thread receivingThread;
    private boolean isReceiving = false;

    private void receiver() {
        if (receivingThread != null && receivingThread.isAlive()) {

            return;
        }
        receivingThread = new Thread() {
            @Override
            public void run() {
                isReceiving = true;
                while (isReceiving) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        if (clientSocket == null)
                            clientSocket = new DatagramSocket();
                        clientSocket.receive(receivePacket);
                        final String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

                        Log.e("sentence", sentence);

                        if (sentence.startsWith("SpeechCommandOK")) {
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    String str = sentence.substring("SpeechCommandOK".length(), sentence.length());
                                    final EditText txtTextInput = (EditText) findViewById(R.id.txtTextInput);

                                    boolean isOnlySpeechRespond=false;
                                    if (txtTextInput != null){
                                        if(str.startsWith(" @@speechOnly@@")){
                                            str=str.substring(" @@speechOnly@@".length(),str.length());
                                            isOnlySpeechRespond=true;
                                        }
                                        if(!txtTextInput.getText().toString().equals(str))
                                            txtTextInput.setText("");
                                    }

                                    if (txtTextInput != null){


                                        if(!txtTextInput.getText().toString().equals(str))
                                            txtTextInput.setText("");
                                    }
                                    if (txtSpeechInput != null) {
                                        txtSpeechInput.setText("");
                                    }

                                    showDialog(str,(isOnlySpeechRespond)? "Speech Successfully executed":"Successfully executed");

                                }
                            });
                        } else if (sentence.startsWith("SpeechCommandNotOK")) {
                            runOnUiThread(new Thread() {
                                @Override
                                public void run() {
                                    String str = sentence.substring("SpeechCommandNotOK".length(), sentence.length());
                                    showDialog(str, "Command not executed");

                                }
                            });
                        }


                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                receivingThread = null;

            }
        };
        receivingThread.start();
    }

    private void showDialog(String firstPar, String text) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(text)
//                .setCancelable(true)
//                .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
//                .setIconAttribute(android.R.attr.alertDialogIcon)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                })
//                .show()        ;

        Drawable iconWrong = getResources().getDrawable(R.drawable.wrong);
        Drawable iconTrue = getResources().getDrawable(R.drawable.corect);
        Drawable iconSpeech = getResources().getDrawable(R.drawable.sp);

        Drawable usingIcon=null;
        if (text.startsWith("Speech ")){
            usingIcon= iconSpeech;
            text.substring("Speech ".length(),text.length());

        }else if(text.equals("Successfully executed")){
            usingIcon=iconTrue;
        }else{
            usingIcon=iconWrong;

        }
        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.gray)
                .setIcon( (usingIcon!=null)? usingIcon : iconTrue)
                .setTitle(text)
                .setMessage(firstPar)
                .show();
    }

    public void sendText(View v) {
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    String input;

                    @Override
                    protected void onPreExecute() {
                        if (txtSpeechInput != null)
                            input = txtSpeechInput.getText().toString();
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (input != null && !input.replaceAll(" ", "").equals("")) {
                            for (InetAddress inetAddress : AutoConnection.usingInetAddress)
                                try {
                                    sendData("speech@@@" + (input), inetAddress, AutoConnection.port);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(SpeechActivity.this, "Sended", Toast.LENGTH_SHORT).show();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });


    }

    public void notSendText(View v) {

        runOnUiThread(new Thread() {
            @Override
            public void run() {
                txtSpeechInput.setText("");

            }
        });

    }

    public void keyboardFunction(View v) {

        switchToKeyboard();


    }

    public void speechFunction(View v) {


        switchToSpeech();
    }


    public void sendTextOnSwitchText(final View v) {
        new Thread() {
            @Override
            public void run() {
                final Button cor = (Button) findViewById(R.id.sendText), wrong = (Button) findViewById(R.id.notSendText);
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        cor.setBackgroundResource(R.drawable.corect_abc);
                        cor.setEnabled(false);
                        wrong.setBackgroundResource(R.drawable.wrong_abc);
                        wrong.setEnabled(false);

                    }
                });

                try {
                    Thread.sleep(3000);
                    runOnUiThread(new Thread() {
                        @Override
                        public void run() {
                            cor.setBackgroundResource(R.drawable.corect);
                            cor.setEnabled(true);
                            wrong.setBackgroundResource(R.drawable.wrong);
                            wrong.setEnabled(true);

                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        final EditText txtTextInput = (EditText) findViewById(R.id.txtTextInput);
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {
                    String input;

                    @Override
                    protected void onPreExecute() {
                        if (txtSpeechInput != null)
                            input = txtTextInput.getText().toString().replaceAll("@", "");
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (input != null && !input.replaceAll(" ", "").equals("")) {
                            for (InetAddress inetAddress : AutoConnection.usingInetAddress)
                                try {
                                    sendData("speech@@@" + (input), inetAddress, AutoConnection.port);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Toast.makeText(SpeechActivity.this, "Sended", Toast.LENGTH_SHORT).show();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });

    }

    public void notSendTextOnSwitchText(View v) {

        final EditText txtTextInput = (EditText) findViewById(R.id.txtTextInput);
        runOnUiThread(new Thread() {
            @Override
            public void run() {
                txtTextInput.setText("");

            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

}