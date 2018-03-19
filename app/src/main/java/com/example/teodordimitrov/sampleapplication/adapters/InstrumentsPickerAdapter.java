package com.example.teodordimitrov.sampleapplication.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.teodordimitrov.sampleapplication.R;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Instrument adapter representing every row in the instruments recycler view.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class InstrumentsPickerAdapter extends RecyclerView.Adapter<InstrumentsPickerAdapter.InstrumentPickerViewHolder> {

	public List<Instrument> getSelectedInstruments () {
		List<Instrument> instrumentsList = new ArrayList<>();
		Map<Instrument, Boolean> instrumentsMap = checkedInstrumentsMap;
		for (Map.Entry<Instrument, Boolean> checkedInstrumentEntry : instrumentsMap.entrySet()) {
			if (checkedInstrumentEntry.getValue()) {
				instrumentsList.add(checkedInstrumentEntry.getKey());
			}
		}
		return instrumentsList;
	}

	private Map<Instrument, Boolean> checkedInstrumentsMap;
	private List<Instrument> instrumentsList;
	private boolean updateOnlyInstruments;
	private Context context;

	public InstrumentsPickerAdapter (Context context, List<Instrument> instrumentsList) {
		this.context = context;
		this.instrumentsList = instrumentsList;
		this.checkedInstrumentsMap = new LinkedHashMap<>();
		updateOnlyInstruments = false;
	}

	@NonNull
	@Override
	public InstrumentPickerViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		View instrumentView = inflater.inflate(R.layout.row_instrument_picker, parent, false);

		return new InstrumentPickerViewHolder(instrumentView);
	}

	@Override
	public void onBindViewHolder (@NonNull InstrumentPickerViewHolder holder, int position) {
		Instrument instrument = instrumentsList.get(position);
		setHolderValues(holder, instrument);
		boolean isInstrumentChecked = checkedInstrumentsMap.get(instrument);
		holder.addInstrumentCheckBox.setChecked(isInstrumentChecked);
	}

	public boolean getUpdateOnlyInstruments () {
		return updateOnlyInstruments;
	}

	public void setInstruments (List<Instrument> instrumentsList, List<Instrument> usersInstrumentsList) {
		this.instrumentsList = instrumentsList;
		setInstrumentsMap(usersInstrumentsList);
		notifyDataSetChanged();
		updateOnlyInstruments = true;
	}

	public void setInstruments (List<Instrument> instrumentsList) {
		this.instrumentsList = instrumentsList;
		notifyDataSetChanged();
	}

	private void setHolderValues (InstrumentPickerViewHolder holder, Instrument instrument) {
		holder.instrumentNameTextView.setText(instrument.getInstrumentName());
		holder.instrumentPriceTextView.setText(String.valueOf(instrument.getCurrentPrice()));
	}

	@Override
	public int getItemCount () {
		return instrumentsList.size();
	}

	private void setInstrumentsMap (List<Instrument> usersInstrumentsList) {
		for (Instrument instrument : instrumentsList) {
			boolean doesUserHaveInstrument = usersInstrumentsList.contains(instrument);
			checkedInstrumentsMap.put(instrument, doesUserHaveInstrument);
		}
	}

	public void setUpdateOnlyInstruments (boolean updateOnlyInstruments) {
		this.updateOnlyInstruments = updateOnlyInstruments;
	}

	class InstrumentPickerViewHolder extends RecyclerView.ViewHolder {

		@BindView (R.id.instrument_picker_row_name_text)
		TextView instrumentNameTextView;
		@BindView (R.id.instrument_picker_row_price_text)
		TextView instrumentPriceTextView;
		@BindView (R.id.instrument_picker_select_check_box)
		CheckBox addInstrumentCheckBox;

		InstrumentPickerViewHolder (View instrumentView) {
			super(instrumentView);
			ButterKnife.bind(this, instrumentView);
		}

		@OnCheckedChanged (R.id.instrument_picker_select_check_box)
		void selectInstrument (CompoundButton checkBox, boolean checked) {
			Instrument instrument = instrumentsList.get(getAdapterPosition());
			checkedInstrumentsMap.put(instrument, checked);
		}

	}

}
