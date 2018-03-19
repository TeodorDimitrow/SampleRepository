package com.example.teodordimitrov.sampleapplication.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsPickerAdapter;
import com.example.teodordimitrov.sampleapplication.adapters.InstrumentsUserAdapter;
import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;
import com.example.teodordimitrov.sampleapplication.callbacks.OnBackPressedListener;
import com.example.teodordimitrov.sampleapplication.callbacks.OnInstrumentsRemovedListener;
import com.example.teodordimitrov.sampleapplication.constants.Constants;
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
 * Instruments fragment which represents the users' instruments
 * and all other available.
 *
 * @author teodor.dimitrov on 19.3.2018 г..
 */

public class InstrumentsFragment extends Fragment implements InstrumentProvider.OnInstrumentsUpdatedListenerListener, OnInstrumentsRemovedListener {

	private static final Integer HANDLER_MSG_CODE_UPDATE_INSTRUMENTS = 1;
	private static final Integer HANDLER_INSTRUMENTS_REFRESH_RATE = 3250;

	/**
	 * The recommended usage of a handler. (Lint documentation).
	 * <p>
	 * The class must be static or we are exposed to memory leaks.
	 * Using a weak reference to not prevent garbage collection.
	 */
	private static class WeakReferenceHandler extends Handler {

		private final WeakReference<InstrumentsFragment> instrumentsFragmentWeakReference;

		private WeakReferenceHandler (InstrumentsFragment fragmentReference) {
			instrumentsFragmentWeakReference = new WeakReference<>(fragmentReference);
		}

		@Override
		public void handleMessage (Message msg) {
			InstrumentsFragment fragmentReference = instrumentsFragmentWeakReference.get();
			if (fragmentReference != null) {
				if (msg.what == HANDLER_MSG_CODE_UPDATE_INSTRUMENTS) {
					if (fragmentReference.areUserInstrumentsPresented) {
						fragmentReference.updateInstrumentPrices();
					} else {
						fragmentReference.instrumentsProvider.getAllInstruments();
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

	private InstrumentProvider instrumentsProvider;
	private InstrumentsPickerAdapter instrumentsPickerAdapter;
	private InstrumentsUserAdapter instrumentsUserAdapter;
	private WeakReferenceHandler handler;
	private List<Instrument> userInstrumentsList;
	private OnBackPressedListener onBackPressedListener;

	private boolean areUserInstrumentsPresented;

	@Nullable
	@Override
	public View onCreateView (LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_instruments, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated (View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		baseFragmentSetup();
		showFabButton();
		areUserInstrumentsPresented = true;
	}

	@Override
	public void onStop () {
		super.onStop();
		handler.removeCallbacksAndMessages(null);
		instrumentsProvider.cancelPriceUpdate();
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

	/**
	 * If the fab button is visible the user can leave on back button.
	 * The same is the case where he is on the main screen.
	 *
	 * @return
	 */
	public boolean getShouldExitOnBack () {
		return areUserInstrumentsPresented;
	}

	public static InstrumentsFragment newInstance () {
		return new InstrumentsFragment();
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		try {
			onBackPressedListener = (OnBackPressedListener) context;
		} catch (ClassCastException e) {
			Log.e(Constants.TAG, "The class must implement OnBackPressedListener", e);
		}

	}

	@OnClick (R.id.instruments_add_instrument_fab_button)
	public void showAllInstruments (View view) {
		handler.sendEmptyMessage(HANDLER_MSG_CODE_UPDATE_INSTRUMENTS);
		signOutTextView.setText(StringUtils.EMPTY);
		instrumentsPickerAdapter.clearChecked();
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
			if (userInstrumentsList.isEmpty()) {
				//Last removed callback will not be triggered since they were unchecked from the picker adapter
				showFabButton();
				return;
			}
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
			onBack();
		} else {
			onBackPressedListener.onBack(true);
		}
	}

	public List<Long> getInstrumentsIds () {
		List<Long> instrumentsIds = new ArrayList<>();
		for (Instrument instrument : userInstrumentsList) {
			instrumentsIds.add(instrument.getId());
		}
		return instrumentsIds;
	}

	public void onBack () {
		signOutTextView.setText(R.string.sign_out);
		instrumentsPickerAdapter.setUpdateOnlyInstruments(false);
		if (userInstrumentsList.isEmpty()) {
			showFabButton();
		} else {
			showUserInstruments();
		}
	}

	private void baseFragmentSetup () {
		areUserInstrumentsPresented = true;
		BaseApplication.getBaseComponent().inject(this);
		userInstrumentsList = new ArrayList<>();
		instrumentsProvider = new InstrumentProvider(this);
		initRecyclerView();
		initHandler();
	}

	/**
	 * Updates and sets picker adapter to the instruments recycler view.
	 * TODO if the user opens and closes without adding items and there are none added, the animation will not be displayed because of line 238.
	 *
	 * @param instrumentsList
	 */
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

	private void showAllInstruments () {
		instrumentsProvider.getAllInstruments();
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
		instrumentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		instrumentsRecyclerView.setAdapter(instrumentsUserAdapter);
	}

	private void initAdapters () {
		instrumentsPickerAdapter = new InstrumentsPickerAdapter(new ArrayList<>());
		instrumentsUserAdapter = new InstrumentsUserAdapter(this, userInstrumentsList);
	}

	private void updateInstrumentPrices () {
		List<Long> instrumentsIds = getInstrumentsIds();
		instrumentsProvider.updateInstrumentPrices(instrumentsIds);
	}

	private void initHandler () {
		handler = new WeakReferenceHandler(this);
		handler.sendEmptyMessage(HANDLER_MSG_CODE_UPDATE_INSTRUMENTS);
	}
}
