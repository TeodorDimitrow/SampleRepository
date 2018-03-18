package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.teodordimitrov.sampleapplication.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Instruments activity.
 * Main activity showing the user his instruments and
 * giving him an option to add new ones.
 *
 * @author teodor.dimitrov
 */
public class InstrumentsActivity extends Activity {

	public static final String EXTRA_HAS_LOGGED_OUT = "logout";

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		ButterKnife.bind(this);
	}

	@OnClick (R.id.instruments_logout_button)
	public void performLogout (View view) {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(EXTRA_HAS_LOGGED_OUT, true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}
}
