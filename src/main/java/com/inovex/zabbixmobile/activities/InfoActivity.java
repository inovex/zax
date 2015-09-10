package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebView;

import com.inovex.zabbixmobile.R;

/**
 * Displays a web view for the about.html content
 */
public class InfoActivity extends AppCompatActivity {

	private WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.info);

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(tb);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		wv = (WebView) findViewById(R.id.webVabout);
		wv.loadUrl("file:///android_asset/about.html");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					if (wv.canGoBack()) {
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
