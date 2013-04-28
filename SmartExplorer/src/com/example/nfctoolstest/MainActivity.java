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


package com.example.nfctoolstest;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * 
 * Boilerplate activity selector.
 * 
 * 
 *
 */

public class MainActivity extends Activity {
	
    private static final String TAG = MainActivity.class.getName();
    TextView textView1 = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_main);
		//textView1 = (TextView) findViewById(R.id.textview1);
		//textView1.setText("Hello world from activity");

    }
    
//    public void writer(View view) {
//    	Log.d(TAG, "Show tag writer");
//    	
//    	Intent intent = new Intent(this, DefaultNfcTagWriterActivity.class);
//    	startActivity(intent);
//    }

    public void reader(View view) {
    	Log.d(TAG, "Show reader");
    	
    	Intent intent = new Intent(this, DefaultNfcReaderActivity.class);
    	startActivity(intent);
    }
//    
//    public void beamer(View view) {
//    	Log.d(TAG, "Show beam writer");
//    	
//    	Intent intent = new Intent(this, DefaultNfcBeamWriterActivity.class);
//    	startActivity(intent);
//    }
//    
}
