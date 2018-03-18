package com.example.teodordimitrov.sampleapplication.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Instruments api call mock response bean.
 * Return the recently updated instruments.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class InstrumentsResponse {

	@SerializedName ("instrumentsList")
	private List<Instrument> instrumentsList;

	public List<Instrument> getInstrumentsList () {
		return instrumentsList;
	}

	public void setInstrumentsList (List<Instrument> instrumentsList) {
		this.instrumentsList = instrumentsList;
	}
}
