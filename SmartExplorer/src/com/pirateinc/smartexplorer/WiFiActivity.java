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

public class WiFiActivity extends Activity {

	String nwSSID = "";
	String nwPass = "";
	EditText wifiSSID ;
	EditText wifiPass ;
	Button submit;
	Button writeTagbtn;
	WifiManager mWifiManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wi_fi);


		wifiSSID = ((EditText) findViewById(R.id.editText1));
		wifiPass = ((EditText) findViewById(R.id.editText2));
//		wifiSSID.setText(nwSSID);
//		wifiPass.setText(nwPass);

//		submit = (Button)findViewById(R.id.submit);
		writeTagbtn = (Button)findViewById(R.id.write_tag);

		mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		
		writeTagbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// TODO Auto-generated method stub
				nwSSID = wifiSSID.getText().toString();
				nwPass = wifiPass.getText().toString();
				
				 Intent returnIntent = new Intent();
				 returnIntent.putExtra("nwSSID",nwSSID);
				 returnIntent.putExtra("nwPass",nwPass);
				 setResult(RESULT_OK,returnIntent);     
				 finish();
				
			}
		});

	//	submit.setOnClickListener(new OnClickListener() {

	//		@Override
	//		public void onClick(View v) {

				//nwSSID = wifiSSID.getText().toString();
				//nwPass = wifiPass.getText().toString();
				
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			if (requestCode == 1) {

	    	     if(resultCode == RESULT_OK)  {      
	    	         nwSSID=data.getStringExtra("tagSSID");
	    	         nwPass=data.getStringExtra("tagPass");
	    	         
	    	         WifiConfiguration mConf = new WifiConfiguration();

	 				mConf.SSID = "\"" + nwSSID + "\"";
	 				mConf.preSharedKey = "\"" + nwPass + "\"";


	 				System.out.println("mConf is- " + mConf);
	 				mWifiManager.addNetwork(mConf);

	 				List<WifiConfiguration> list = new ArrayList<WifiConfiguration>();
	 				list = mWifiManager.getConfiguredNetworks();
	 				System.out.println("list size- " + list.size());
	 				for( WifiConfiguration i : list ) {
	 					if(i.SSID != null && i.SSID.equals("\"" + nwSSID + "\"")) {
	 						mWifiManager.disconnect();
	 						mWifiManager.enableNetwork(i.networkId, true);
	 						mWifiManager.reconnect();               

	 						break;
	 					}
	    	         
	    	     }   
	    	}         
		}
				
				           
				}


//			}
//		});



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

}
