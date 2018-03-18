package com.example.teodordimitrov.sampleapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Instrument adapter representing every row in the instruments recycler view.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class InstrumentsAdapter extends RecyclerView.Adapter<InstrumentsAdapter.InstrumentViewHolder> {

	private Map<Instrument, Boolean> checkedInstrumentsMap;
	private List<Instrument> instrumentsList;
	private boolean isInstrumentPickerAdapter;

	public InstrumentsAdapter (Activity activity, List<Instrument> instrumentsList, boolean isInstrumentPickerAdapter) {
		this.instrumentsList = instrumentsList;
		this.isInstrumentPickerAdapter = isInstrumentPickerAdapter;
		if (isInstrumentPickerAdapter) {
			initInstrumentsMap();
		}
	}

	@NonNull
	@Override
	public InstrumentViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the custom layout
		View instrumentView = inflater.inflate(R.layout.row_instrument, parent, false);

		// Return a new holder instance
		return new InstrumentViewHolder(instrumentView);
	}

	@Override
	public void onBindViewHolder (@NonNull InstrumentViewHolder holder, int position) {
		Instrument instrument = instrumentsList.get(position);
		holder.instrumentNameTextView.setText(instrument.getInstrumentName());
		holder.instrumentPriceTextView.setText(String.valueOf(instrument.getCurrentPrice()));
		if (!isInstrumentPickerAdapter) {
			if (!holder.removeInstrumentButton.isEnabled()) {
				holder.removeInstrumentButton.setEnabled(true);
				updatePrices();
			}
		}

	}

	private void updatePrices () {

	}

	@Override
	public int getItemCount () {
		return instrumentsList.size();
	}

	private void initInstrumentsMap () {
		this.checkedInstrumentsMap = new HashMap<>();
		for (Instrument instrument : instrumentsList) {
			checkedInstrumentsMap.put(instrument, false);
		}
	}

	class InstrumentViewHolder extends RecyclerView.ViewHolder {

		@BindView (R.id.instrument_row_name_text_view)
		TextView instrumentNameTextView;
		@BindView (R.id.instrument_row_price_text_view)
		TextView instrumentPriceTextView;
		@BindView (R.id.instrument_remove_image_button)
		ImageButton removeInstrumentButton;
		@BindView (R.id.instrument_select_check_box)
		CheckBox addInstrumentCheckBox;

		InstrumentViewHolder (View instrumentView) {
			super(instrumentView);
			ButterKnife.bind(this, instrumentView);
			setViewsProperties();
		}

		/**
		 * Removes the instrument from the
		 * user's collection.
		 * The button is disabled after removal since
		 * there is a 500 milliseconds period where the user can click again the
		 * fading out view, which ends with @{@link IndexOutOfBoundsException}
		 *
		 * @param view Click performed on view
		 */
		@OnClick (R.id.instrument_remove_image_button)
		void removeInstrument (View view) {
			instrumentsList.remove(getAdapterPosition());
			removeInstrumentButton.setEnabled(false);
			notifyItemRemoved(getAdapterPosition());
			notifyItemRangeChanged(getAdapterPosition(), instrumentsList.size());
		}

		@OnCheckedChanged (R.id.instrument_select_check_box)
		void selectInstrument (CompoundButton checkBox, boolean checked) {
			Instrument instrument = instrumentsList.get(getAdapterPosition());
			checkedInstrumentsMap.put(instrument, checked);
		}

		private void setViewsProperties () {
			if (isInstrumentPickerAdapter) {
				instrumentPriceTextView.setBackgroundColor(Color.GRAY);
				removeInstrumentButton.setVisibility(View.GONE);
				addInstrumentCheckBox.setVisibility(View.VISIBLE);
			} else {
				removeInstrumentButton.setVisibility(View.VISIBLE);
				addInstrumentCheckBox.setVisibility(View.GONE);
			}
		}
	}

}
