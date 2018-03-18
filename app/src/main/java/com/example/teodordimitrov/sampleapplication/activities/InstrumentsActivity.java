package com.example.teodordimitrov.sampleapplication.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsAdapter;
import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;
import com.example.teodordimitrov.sampleapplication.providers.InstrumentPriceProvider;

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
public class InstrumentsActivity extends Activity implements InstrumentPriceProvider.OnPricesUpdatedListener {

	public static final String EXTRA_HAS_LOGGED_OUT = "logout";
	private static final Integer HANDLER_MSG_CODE_UPDATE_PRICES = 1;
	private static final Integer HANDLER_PRICES_REFRESH_RATE = 3000;

	/**
	 * This is the recommended usage of a handler. (Lint documentation).
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
				if (msg.what == HANDLER_MSG_CODE_UPDATE_PRICES) {
					instrumentsActivity.updatePrices();
					sendEmptyMessageDelayed(HANDLER_MSG_CODE_UPDATE_PRICES, HANDLER_PRICES_REFRESH_RATE);
				}
			}
		}
	}

	@BindView (R.id.instruments_recycler_view)
	protected RecyclerView instrumentsRecyclerView;

	protected InstrumentPriceProvider instrumentPriceProvider;

	private InstrumentsAdapter instrumentsAdapter;
	private WeakReferenceHandler handler;
	private List<Instrument> userInstrumentsList;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruments);
		BaseApplication.getBaseComponent().inject(this);
		ButterKnife.bind(this);
		initRecyclerView();
		initHandler();
		instrumentPriceProvider = new InstrumentPriceProvider(this);
	}

	@Override
	protected void onStop () {
		super.onStop();
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onPricesUpdated (List<Instrument> instrumentsList) {
		instrumentsAdapter.setUpdatePrices(instrumentsList);
	}

	@OnClick (R.id.instruments_logout_button)
	public void performLogout (View view) {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(EXTRA_HAS_LOGGED_OUT, true);
		startActivity(intent);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	public List<Long> getInstrumentsIds () {
		List<Long> instrumentsIds = new ArrayList<>();
		for (Instrument instrument : userInstrumentsList) {
			instrumentsIds.add(instrument.getId());
		}
		return instrumentsIds;
	}

	private void initRecyclerView () {
		userInstrumentsList = prepareInstrument();
		instrumentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		instrumentsAdapter = new InstrumentsAdapter(userInstrumentsList, false);
		instrumentsRecyclerView.setAdapter(instrumentsAdapter);
	}

	private void updatePrices () {
		List<Long> instrumentsIds = getInstrumentsIds();
		instrumentPriceProvider.updatePrices(instrumentsIds);
	}

	private List<Instrument> prepareInstrument () {
		List<Instrument> instruments = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			instruments.add(new Instrument(i, 12 + i, "Ripple " + i));
		}
		return instruments;
	}

	private void initHandler () {
		handler = new WeakReferenceHandler(this);
		handler.sendEmptyMessage(HANDLER_MSG_CODE_UPDATE_PRICES);
	}

	private void runLayoutAnimation (final RecyclerView recyclerView) {
		final Context context = recyclerView.getContext();
		final LayoutAnimationController controller =
				AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

		recyclerView.setLayoutAnimation(controller);
		recyclerView.getAdapter().notifyDataSetChanged();
		recyclerView.scheduleLayoutAnimation();
	}

}
