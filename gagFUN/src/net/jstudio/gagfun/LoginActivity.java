package net.jstudio.gagfun;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	public void onCreate(Bundle state) {
		super.onCreate(state);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);
		
		
		Button btt_cancel = (Button)findViewById(R.id.cancel);
		btt_cancel.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				setResult(RESULT_CANCELED,data);
				finish();
			}
			
		});
		Button btt_ok = (Button)findViewById(R.id.login);
		btt_ok.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				EditText username = (EditText)findViewById(R.id.username);
				EditText password = (EditText)findViewById(R.id.password);
				data.putExtra("username", username.getText().toString());
				data.putExtra("password", password.getText().toString());
				setResult(RESULT_OK,data);
				finish();
			}
			
		});
	}
}
