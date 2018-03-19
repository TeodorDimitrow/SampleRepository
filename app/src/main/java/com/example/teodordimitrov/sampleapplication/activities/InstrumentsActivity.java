package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsPickerAdapter;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsUserAdapter;
import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;
import com.example.teodordimitrov.sampleapplication.callbacks.OnInstrumentsRemovedListener;
import com.example.teodordimitrov.sampleapplication.providers.InstrumentProvider;
import com.example.teodordimitrov.sampleapplication.util.AnimationUtils;
import com.example.teodordimitrov.sampleapplication.util.StringUtils;

import java.lang.ref.WeakReference;
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
public class InstrumentsActivity extends Activity implements InstrumentProvider.OnInstrumentsUpdatedListenerListener, OnInstrumentsRemovedListener {

	public static final String EXTRA_HAS_LOGGED_OUT = "logout";
	private static final Integer HANDLER_MSG_CODE_UPDATE_INSTRUMENTS = 1;
	private static final Integer HANDLER_INSTRUMENTS_REFRESH_RATE = 2000;

	/**
	 * The recommended usage of a handler. (Lint documentation).
	 * <p>
	 * The class must be static or we are exposed to memory leaks.
	 * Using a weak reference to not prevent garbage collection.
	 */
	private static class WeakReferenceHandler extends Handler {

		private final WeakReference<InstrumentsActivity> instrumentsActivityWeakReference;

		private WeakReferenceHandler (InstrumentsActivity activityReference) {
			instrumentsActivityWeakReference = new WeakReference<>(activityReference);
		}

		@Override
		public void handleMessage (Message msg) {
			InstrumentsActivity instrumentsActivity = instrumentsActivityWeakReference.get();
			if (instrumentsActivity != null) {
				if (msg.what == HANDLER_MSG_CODE_UPDATE_INSTRUMENTS) {
					if (instrumentsActivity.areUserInstrumentsPresented) {
						instrumentsActivity.updateInstrumentPrices();
					} else {
						instrumentsActivity.instrumentProvider.getAllInstruments();
					}
					sendEmptyMessageDelayed(HANDLER_MSG_CODE_UPDATE_INSTRUMENTS, HANDLER_INSTRUMENTS_REFRESH_RATE);
				}
			}
		}
	}

	@BindView (R.id.instruments_recycler_view)
	protected RecyclerView instrumentsRecyclerView;

	@BindView (R.id.instruments_fab_button_container)
	protected LinearLayout fabButtonContainer;

	@BindView (R.id.instruments_bottom_toolbar_layout)
	protected LinearLayout bottomToolbarLayout;

	@BindView (R.id.instruments_add_rearrange_text)
	protected TextView addInstrumentsTextView;

	@BindView (R.id.instruments_sign_out_text)
	protected TextView signOutTextView;

	private InstrumentProvider instrumentProvider;
	private InstrumentsPickerAdapter instrumentsPickerAdapter;
	private InstrumentsUserAdapter instrumentsUserAdapter;
	private WeakReferenceHandler handler;
	private List<Instrument> userInstrumentsList;
	private boolean areUserInstrumentsPresented;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		baseActivitySetup();
		showFabButton();
	}

	@Override
	protected void onStop () {
		super.onStop();
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onInstrumentsRemoved (Instrument instrument) {
		userInstrumentsList.remove(instrument);
		if (userInstrumentsList.isEmpty()) {
			showFabButton();
		}
	}

	@Override
	public void onUserInstrumentsUpdated (List<Instrument> instrumentsList) {
		Instrument.updateInstrumentPrices(userInstrumentsList, instrumentsList);
		instrumentsUserAdapter.setInstruments(userInstrumentsList);
	}

	@Override
	public void onAllInstrumentsUpdated (List<Instrument> instrumentsList) {
		if (userInstrumentsList.isEmpty()) {
			instrumentsRecyclerView.setVisibility(View.VISIBLE);
			hideFabButton();
		}
		setPickerAdapter(instrumentsList);
	}

	public void logout () {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(EXTRA_HAS_LOGGED_OUT, true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	@OnClick (R.id.instruments_add_instrument_fab_button)
	public void showAllInstruments (View view) {
		handler.sendEmptyMessage(HANDLER_MSG_CODE_UPDATE_INSTRUMENTS);
		signOutTextView.setText(StringUtils.EMPTY);
		showAllInstruments();
	}

	/**
	 * Called when the user clicks add/select button
	 * in the bottom right corner.
	 * <p>
	 * areUserInstrumentsPresented is false if
	 * the user is on all instruments screen, and is true if
	 * the user is on his chosen instruments screen.
	 *
	 * @param view the image button
	 */
	@OnClick (R.id.instruments_add_instrument_button)
	public void addInstruments (View view) {
		if (!areUserInstrumentsPresented) {
			signOutTextView.setText(R.string.sign_out);
			userInstrumentsList = instrumentsPickerAdapter.getSelectedInstruments();
			instrumentsPickerAdapter.setUpdateOnlyInstruments(false);
			showUserInstruments();
		} else {
			signOutTextView.setText(StringUtils.EMPTY);
			addInstrumentsTextView.setText(R.string.instruments_select);
			showAllInstruments();
		}
	}

	@OnClick (R.id.instruments_back_button)
	public void onCustomBackPressed (View view) {
		if (!areUserInstrumentsPresented) {
			signOutTextView.setText(R.string.sign_out);
			showUserInstruments();
		} else {
			logout();
		}
	}

	private void baseActivitySetup () {
		BaseApplication.getBaseComponent().inject(this);
		ButterKnife.bind(this);
		userInstrumentsList = new ArrayList<>();
		instrumentProvider = new InstrumentProvider(this);
		initRecyclerView();
		initHandler();
	}

	private void setPickerAdapter (List<Instrument> instrumentsList) {
		if (instrumentsPickerAdapter.getUpdateOnlyInstruments()) {
			instrumentsPickerAdapter.setInstruments(instrumentsList);
		} else {
			instrumentsPickerAdapter.setInstruments(instrumentsList, userInstrumentsList);
		}
		instrumentsPickerAdapter.notifyDataSetChanged();
		if (areUserInstrumentsPresented) {
			instrumentsRecyclerView.setAdapter(instrumentsPickerAdapter);
			AnimationUtils.layoutFallDownAnimation(instrumentsRecyclerView);
			areUserInstrumentsPresented = false;
		}
	}

	private void showUserInstruments () {
		areUserInstrumentsPresented = true;
		addInstrumentsTextView.setText(R.string.instruments_add);
		instrumentsRecyclerView.setAdapter(instrumentsUserAdapter);
		instrumentsUserAdapter.setInstruments(userInstrumentsList);
		instrumentsUserAdapter.notifyDataSetChanged();
		AnimationUtils.layoutFallDownAnimation(instrumentsRecyclerView);
	}

	public List<Long> getInstrumentsIds () {
		List<Long> instrumentsIds = new ArrayList<>();
		for (Instrument instrument : userInstrumentsList) {
			instrumentsIds.add(instrument.getId());
		}
		return instrumentsIds;
	}

	private void showAllInstruments () {
		instrumentProvider.getAllInstruments();
	}

	private void showFabButton () {
		addInstrumentsTextView.setText(R.string.instruments_select);
		fabButtonContainer.setVisibility(View.VISIBLE);
		instrumentsRecyclerView.setVisibility(View.GONE);
		bottomToolbarLayout.setVisibility(View.GONE);
		handler.removeCallbacksAndMessages(null);
	}

	private void hideFabButton () {
		fabButtonContainer.setVisibility(View.GONE);
		instrumentsRecyclerView.setVisibility(View.VISIBLE);
		bottomToolbarLayout.setVisibility(View.VISIBLE);
	}

	private void initRecyclerView () {
		initAdapters();
		instrumentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		instrumentsRecyclerView.setAdapter(instrumentsUserAdapter);
	}

	private void initAdapters () {
		areUserInstrumentsPresented = true;
		instrumentsPickerAdapter = new InstrumentsPickerAdapter(this, new ArrayList<>());
		instrumentsUserAdapter = new InstrumentsUserAdapter(this, userInstrumentsList);
	}

	private void updateInstrumentPrices () {
		List<Long> instrumentsIds = getInstrumentsIds();
		instrumentProvider.updateInstrumentPrices(instrumentsIds);
	}

	private void initHandler () {
		handler = new WeakReferenceHandler(this);
		handler.sendEmptyMessage(HANDLER_MSG_CODE_UPDATE_INSTRUMENTS);
	}

	@Override
	public void onBackPressed () {
		if (!areUserInstrumentsPresented) {
			showUserInstruments();
		} else {
			super.onBackPressed();
		}
	}
}
