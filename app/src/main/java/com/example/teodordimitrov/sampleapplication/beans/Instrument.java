package com.example.teodordimitrov.sampleapplication.beans;

import com.google.gson.annotations.SerializedName;

/**
 * Instrument bean.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class Instrument {

	public static final String FIELD_NAME_ID = "id";

	public static final String FIELD_NAME_CURRENT_PRICE = "currentPrice";

	@SerializedName ("id")
	private long id;

	@SerializedName ("currentPrice")
	private float currentPrice;

	@SerializedName ("instrumentName")
	private String instrumentName;

	public long getId () {
		return id;
	}

	public void setId (long id) {
		this.id = id;
	}

	public Instrument (float currentPrice, String instrumentName) {
		this.currentPrice = currentPrice;
		this.instrumentName = instrumentName;
	}

	public Instrument (long id, float currentPrice, String instrumentName) {
		this.id = id;
		this.currentPrice = currentPrice;
		this.instrumentName = instrumentName;
	}

	public float getCurrentPrice () {
		return currentPrice;
	}

	public void setCurrentPrice (float currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getInstrumentName () {
		return instrumentName;
	}

	public void setInstrumentName (String instrumentName) {
		this.instrumentName = instrumentName;
	}
}
