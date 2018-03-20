package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.callbacks.OnBackPressedListener;
import com.example.teodordimitrov.sampleapplication.fragments.InstrumentsFragment;

/**
 * Instruments activity.
 * Main activity showing the user his instruments and
 * giving him an option to add new ones.
 *
 * @author teodor.dimitrov
 */
public class MainActivity extends Activity implements OnBackPressedListener {

	public static final String EXTRA_HAS_LOGGED_OUT = "logout";

	private InstrumentsFragment instrumentsFragment;

	@Override
	public void onBack (boolean shouldLogout) {
		if (shouldLogout) {
			logout();
		} else {
			finish();
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		initInstrumentsFragment();
	}

	private void initInstrumentsFragment () {
		FragmentManager fragmentManager = getFragmentManager();
		instrumentsFragment = InstrumentsFragment.newInstance();
		fragmentManager.beginTransaction().replace(R.id.instruments_fragment_container, InstrumentsFragment.newInstance()).commit();
	}

	private void logout () {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(MainActivity.EXTRA_HAS_LOGGED_OUT, true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

}
