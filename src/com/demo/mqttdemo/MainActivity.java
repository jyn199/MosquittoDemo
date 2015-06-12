package com.demo.mqttdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.service.MosquittoClientService;
import com.example.mqttdemo.R;

public class MainActivity extends Activity {

	EditText etUsername;
	Button connectMqtt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		etUsername = (EditText)findViewById(R.id.et_username);
		connectMqtt = (Button)findViewById(R.id.connect_mqtt);
		
		connectMqtt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = etUsername.getText().toString();
				if(username == null || username.trim().equals("")){
					Toast.makeText(MainActivity.this, "«Î ‰»Îrtx_id", Toast.LENGTH_SHORT).show();
				}else{
					Intent intent = new Intent();
					intent.putExtra("username", username);
					intent.setClass(MainActivity.this, MosquittoClientService.class);
					startService(intent);
				}
			}
		});
		
	}
}
