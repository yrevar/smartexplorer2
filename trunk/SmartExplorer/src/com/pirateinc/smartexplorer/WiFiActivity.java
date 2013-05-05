package com.pirateinc.smartexplorer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WiFiActivity extends Activity {

	EditText wifiSSID;
	EditText wifiPass;
	Button submit;
	Button writeTagbtn;
	WifiManager mWifiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		Intent myintent = getIntent();
		 
        if(null!=myintent.getExtras()) {
        	
        	String rTagSSID = myintent.getExtras().getString("tagSSID");
        	String rTagPass = myintent.getExtras().getString("tagPass");
        	
        	toast("Wifi Activity Received Input: " + rTagSSID + " " + rTagPass);
        	ConnectWifi(rTagSSID, rTagPass);
        	finish();

        }
        else {
        	
        	toast("Wifi Activity Did not receive input");
        	setContentView(R.layout.activity_wi_fi);
        	wifiSSID = ((EditText) findViewById(R.id.editText1));
    		wifiPass = ((EditText) findViewById(R.id.editText2));
    		
    		// wifiSSID.setText(nwSSID);
    		// wifiPass.setText(nwPass);

    		// submit = (Button)findViewById(R.id.submit);
    		writeTagbtn = (Button) findViewById(R.id.write_tag);
    		writeTagbtn.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {

    				String tNwSSID = "";
    				String tNwPass = "";
    				// TODO Auto-generated method stub
    				tNwSSID = wifiSSID.getText().toString();
    				tNwPass = wifiPass.getText().toString();

    				Intent returnIntent = new Intent();
    				returnIntent.putExtra("nwSSID", tNwSSID);
    				returnIntent.putExtra("nwPass", tNwPass);
    				setResult(RESULT_OK, returnIntent);
    				finish();

    			}
    		});
        }

		// submit.setOnClickListener(new OnClickListener() {

		// @Override
		// public void onClick(View v) {

		// nwSSID = wifiSSID.getText().toString();
		// nwPass = wifiPass.getText().toString();

		// }
		// });

	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				ConnectWifi(data.getStringExtra("tagSSID"), data.getStringExtra("tagPass"));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wi_fi, menu);
		return true;
	}
	
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
	
	private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    

}
