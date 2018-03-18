package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsAdapter;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
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

	@BindView (R.id.instruments_recycler_view)
	RecyclerView instrumentsRecyclerView;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		ButterKnife.bind(this);
		initRecyclerView();
	}

	private void initRecyclerView () {
		instrumentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		instrumentsRecyclerView.setAdapter(new InstrumentsAdapter(this, prepareInstrument(), false));
	}

	@OnClick (R.id.instruments_logout_button)
	public void performLogout (View view) {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(EXTRA_HAS_LOGGED_OUT, true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	private List<Instrument> prepareInstrument () {
		List<Instrument> instruments = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			instruments.add(new Instrument(124.123f, "Ripple"));
		}
		return instruments;
	}
}
