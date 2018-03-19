package com.example.teodordimitrov.sampleapplication.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (!(o instanceof Instrument)) return false;

		Instrument that = (Instrument) o;

		if (id != that.id) return false;
		return instrumentName.equals(that.instrumentName);
	}

	@Override
	public int hashCode () {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + instrumentName.hashCode();
		return result;
	}

	/**
	 * While retrieving the new prices the user might delete
	 * some instruments from their collection.
	 * By not simple using the new list we make sure the order stays the same
	 * and that if the user has deleted something, it will not show again.
	 * <p>
	 * TODO This can be done by SIMPLE intersection. Can also be made to perform much better.
	 *
	 * @param currentList list of instruments which the user has currently
	 * @param updatedList list of updated instruments and their new prices.
	 */
	public static void updateInstrumentPrices (List<Instrument> currentList, List<Instrument> updatedList) {
		for (int i = 0; i < currentList.size(); i++) {
			for (int j = 0; j < updatedList.size(); j++) {
				Instrument currentInstrument = currentList.get(i);
				Instrument updatedInstrument = updatedList.get(j);
				if (currentInstrument.equals(updatedInstrument)) {
					currentInstrument.setCurrentPrice(updatedInstrument.getCurrentPrice());
				}
			}
		}
	}
}
