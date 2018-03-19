package com.example.teodordimitrov.sampleapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;
import com.example.teodordimitrov.sampleapplication.callbacks.OnInstrumentsRemovedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Instruments adapter representing the users chosen instruments and their
 * current <STRONG>accurate</STRONG> prices.
 *
 * @author teodor.dimitrov on 19.3.2018 г..
 */

public class InstrumentsUserAdapter extends RecyclerView.Adapter<InstrumentsUserAdapter.InstrumentUserViewHolder> {

	private List<Instrument> instrumentsList;
	private OnInstrumentsRemovedListener instrumentsRemovedListener;

	public InstrumentsUserAdapter (OnInstrumentsRemovedListener instrumentsRemovedListener, List<Instrument> instrumentsList) {
		this.instrumentsList = instrumentsList;
		this.instrumentsRemovedListener = instrumentsRemovedListener;
		setHasStableIds(true);
	}

	@NonNull
	@Override
	public InstrumentUserViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		View instrumentView = inflater.inflate(R.layout.row_instrument_user, parent, false);

		return new InstrumentUserViewHolder(instrumentView);
	}

	@Override
	public void onBindViewHolder (@NonNull InstrumentUserViewHolder holder, int position) {
		Instrument instrument = instrumentsList.get(position);
		setHolderValues(holder, instrument);
		if (!holder.removeInstrumentButton.isEnabled()) {
			holder.removeInstrumentButton.setEnabled(true);
		}
	}

	@Override
	public long getItemId (int position) {
		return position;
	}

	@Override
	public int getItemViewType (int position) {
		return position;
	}

	public void setInstruments (List<Instrument> usersInstrumentsList) {
		this.instrumentsList = usersInstrumentsList;
		notifyDataSetChanged();
	}

	private void setHolderValues (InstrumentUserViewHolder holder, Instrument instrument) {
		float oldInstrumentPrice = -1f;
		if (!holder.instrumentPriceTextView.getText().toString().isEmpty()) {
			String oldInstrumentPriceString = holder.instrumentPriceTextView.getText().toString();
			oldInstrumentPrice = Float.parseFloat(oldInstrumentPriceString);
		}
		if (oldInstrumentPrice != -1f) {
			if (oldInstrumentPrice > instrument.getCurrentPrice()) {
				holder.instrumentPriceTextView.setBackgroundColor(Color.RED);
				holder.instrumentPriceTextView.setTextColor(Color.WHITE);
			} else if (oldInstrumentPrice < instrument.getCurrentPrice()) {
				holder.instrumentPriceTextView.setBackgroundColor(Color.GREEN);
				holder.instrumentPriceTextView.setTextColor(Color.BLACK);
			} else {
				holder.instrumentPriceTextView.setBackgroundColor(Color.GRAY);
				holder.instrumentPriceTextView.setTextColor(Color.WHITE);
			}
		}
		holder.instrumentNameTextView.setText(instrument.getInstrumentName());
		holder.instrumentPriceTextView.setText(String.valueOf(instrument.getCurrentPrice()));
	}

	@Override
	public int getItemCount () {
		return instrumentsList.size();
	}

	class InstrumentUserViewHolder extends RecyclerView.ViewHolder {

		@BindView (R.id.instrument_user_row_name_text)
		TextView instrumentNameTextView;
		@BindView (R.id.instrument_user_row_price_text)
		TextView instrumentPriceTextView;
		@BindView (R.id.instrument_user_remove_image_button)
		ImageButton removeInstrumentButton;

		InstrumentUserViewHolder (View instrumentView) {
			super(instrumentView);
			ButterKnife.bind(this, instrumentView);
		}

		/**
		 * Removes the instrument from the
		 * user's collection.
		 * The button is disabled after removal since
		 * there is a 200 milliseconds period where the user can click again (almost impossible) the
		 * fading out view, which sometimes ends with @{@link IndexOutOfBoundsException}
		 *
		 * @param view Click performed on view
		 */
		@OnClick (R.id.instrument_user_remove_image_button)
		void removeInstrument (View view) {
			removeInstrumentButton.setEnabled(false);
			instrumentsRemovedListener.onInstrumentsRemoved(instrumentsList.remove(getAdapterPosition()));
			notifyDataSetChanged();
		}

	}
}
