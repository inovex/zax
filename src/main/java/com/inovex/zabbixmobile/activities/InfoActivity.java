package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;
import com.inovex.zabbixmobile.R;

/**
 * Created by felix on 13/05/15.
 */
public class InfoActivity extends AppCompatActivity {

	private WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.info);

		wv = (WebView) findViewById(R.id.webVabout);
		wv.loadUrl("file:///android_asset/about.html");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch (keyCode){
				case KeyEvent.KEYCODE_BACK:
					if(wv.canGoBack()){
						wv.goBack();
					} else {
						finish();
					}
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
