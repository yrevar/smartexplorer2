/***************************************************************************
 * 
 * This file is part of the 'NDEF Tools for Android' project at
 * http://code.google.com/p/ndef-tools-for-android/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ****************************************************************************/


package com.pirateinc.smartexplorer;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * Boilerplate activity selector.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class SmartExplorerMainActivity extends Activity {
	
	private static final String TAG = "smartexplorer";
	private boolean mResumed = false;
	private boolean mWriteMode = false;
    NfcAdapter mNfcAdapter;
    EditText mNote;
    String nwSSID = "Default";
	String nwPass = "1234";
	
	Boolean updateWifiPara = false;
	
	PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;
    NdefRecord wifitextRecord;
    WifiManager mWifiManager;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
		setContentView(R.layout.activity_main);
		findViewById(R.id.write_tag).setOnClickListener(mTagWriter);
		mNote = ((EditText) findViewById(R.id.note));
        mNote.addTextChangedListener(mTextWatcher);
        
        Button WiFibtn = (Button) findViewById(R.id.button1);
        WiFibtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent WiFibtnintent = new Intent(v.getContext(), WiFiActivity.class);
				startActivityForResult(WiFibtnintent, 1);
				
			}
		});
	

        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for reading a note from a tag.
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/c_wifi");
        } catch (MalformedMimeTypeException e) { }
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { tagDetected };
    }
    

    public void reader(View view) {
    	Log.d(TAG, "Show reader");
    	
    	Intent intent = new Intent(this, SENfcReaderActivity.class);
    	startActivity(intent);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	  if (requestCode == 1) {

    	     if(resultCode == RESULT_OK){      
    	         nwSSID=data.getStringExtra("nwSSID");
    	         nwPass=data.getStringExtra("nwPass");
    	         
    	        toast("From Wifi Para:" + "ssid=" + nwSSID + ", pass=" + nwPass);
    	        updateWifiPara = true;
    	     }
    	     if (resultCode == RESULT_CANCELED) {    
    	         //Write your code if there's no result
    	     }
    	  }
    	}//onActivityResult
    
    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        // Sticky notes received from Android
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            NdefMessage[] messages = getNdefMessages(getIntent());
            byte[] payload = messages[0].getRecords()[0].getPayload();
            promptForContent(messages[0]);
//            setNoteBody(new String(payload));
            setIntent(new Intent()); // Consume this intent.
        }
        enableNdefExchangeMode();
        
        if(updateWifiPara == true) {
     	// Write to a tag for as long as the dialog is shown.
        disableNdefExchangeMode();
        enableTagWriteMode();

        new AlertDialog.Builder(SmartExplorerMainActivity.this).setTitle("Touch tag to write")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        disableTagWriteMode();
                        enableNdefExchangeMode();
                    }
                }).create().show();
        
        updateWifiPara = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(this);
        updateWifiPara = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            promptForContent(msgs[0]);
        }

        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(
            		NfcAdapter.EXTRA_TAG);
            //writeTag(getNoteAsNdef(), detectedTag);
            writeTag(getWifiPAraAsNdef(), detectedTag);
        }
    }

    @SuppressLint("NewApi")
	private void promptForContent(final NdefMessage msg) {
    	
    	String tagSSID;
    	String tagPass;
    	
    	
//    	if (msg.getRecords()[0].equals(wifitextRecord)) {
    		
    
    	String wificredentialString = new String(msg.getRecords()[0].getPayload());
    	String[] split = wificredentialString.split(";");
    	tagSSID = split[0];
    	tagPass = split[1];
    	
    	/*new AlertDialog.Builder(this).setTitle("Connecting ssid: \"" + tagSSID + "\", pass: \"" + tagPass + "\"")
        .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                String body = new String(msg.getRecords()[0].getPayload());
                setNoteBody(body);
            }
        })
        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                
            }
        }).show();
    	*/
    	
    	// Prepare intent which is triggered if the
    	// notification is selected

    	Intent intent = new Intent(this, SmartExplorerMainActivity.class);
    	PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
    	// Build notification
    	// Actions are just fake
    	Notification noti = new Notification.Builder(this)
    	        .setContentTitle("SmartExplorer wifi nw connected")
    	        .setContentText("SSID: " + tagSSID)
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setContentIntent(pIntent)
    	        .addAction(R.drawable.ic_launcher, "Disconnect", pIntent)
    	        .addAction(R.drawable.ic_launcher, "Program", pIntent).build();
    	    
    	  
    	NotificationManager notificationManager = 
    	  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    	// Hide the notification after its selected
    	noti.flags |= Notification.FLAG_AUTO_CANCEL;

    	notificationManager.notify(0, noti);
    	
    	ConnectWifi(tagSSID, tagPass);
    	/*Intent WiFibtnintent = new Intent(SmartExplorerMainActivity.this, WiFiActivity.class);
    	
    	WiFibtnintent.putExtra("tagSSID", tagSSID);
    	WiFibtnintent.putExtra("tagPass", tagPass);
    	startActivity(WiFibtnintent);*/
    	    	
//    	}
    	
    	
    }
    
    private NdefMessage getNoteAsNdef() {
        byte[] textBytes = mNote.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/c_wifi".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }
    
    private NdefMessage getWifiPAraAsNdef() {
    
    	String wifiParaFormat = nwSSID  + ";" + nwPass;
        byte[] textBytes = wifiParaFormat.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/c_wifi".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }

    private void setNoteBody(String body) {
        Editable text = mNote.getText();
        text.clear();
        text.append(body);
    }

    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }
        } else {
            Log.d(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundNdefPush(SmartExplorerMainActivity.this, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode() {
        mNfcAdapter.disableForegroundNdefPush(this);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
            tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast("Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast("Wrote message to pre-formatted tag.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast("Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast("Failed to format tag.");
                        return false;
                    }
                } else {
                    toast("Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            toast("Failed to write tag");
        }

        return false;
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    
    
    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (mResumed) {
                mNfcAdapter.enableForegroundNdefPush(SmartExplorerMainActivity.this, getNoteAsNdef());
            }
        }
    };
    
    private void ConnectWifi(String inSSID, String inPass) {
    	
		WifiConfiguration mConf = new WifiConfiguration();

		mConf.SSID = "\"" + inSSID + "\"";
		mConf.preSharedKey = "\"" + inPass + "\"";

		System.out.println("mConf is- " + mConf);
		mWifiManager.addNetwork(mConf);

		List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
		list = mWifiManager.getConfiguredNetworks();
		System.out.println("list size- " + list.size());
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"" + inSSID + "\"")) {
				mWifiManager.disconnect();
				mWifiManager.enableNetwork(i.networkId, true);
				mWifiManager.reconnect();

				break;
			}

		}
	}

    private View.OnClickListener mTagWriter = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // Write to a tag for as long as the dialog is shown.
            disableNdefExchangeMode();
            enableTagWriteMode();

            new AlertDialog.Builder(SmartExplorerMainActivity.this).setTitle("Touch tag to write")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            disableTagWriteMode();
                            enableNdefExchangeMode();
                        }
                    }).create().show();
        }
    };
}

/*
 *
 * //    public void writer(View view) {
//    	Log.d(TAG, "Show tag writer");
//    	
//    	Intent intent = new Intent(this, DefaultNfcTagWriterActivity.class);
//    	startActivity(intent);
//    }
    
//  
//  public void beamer(View view) {
//  	Log.d(TAG, "Show beam writer");
//  	
//  	Intent intent = new Intent(this, DefaultNfcBeamWriterActivity.class);
//  	startActivity(intent);
//  }
//  

 * 
*/