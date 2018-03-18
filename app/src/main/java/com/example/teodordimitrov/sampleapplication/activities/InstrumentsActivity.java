package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.os.Bundle;

import com.example.teodordimitrov.sampleapplication.R;

import butterknife.ButterKnife;

/**
 * Instruments activity.
 * Main activity showing the user his instruments and
 * giving him an option to add new ones.
 *
 * @author teodor.dimitrov
 */
public class InstrumentsActivity extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		ButterKnife.bind(this);
	}
}
