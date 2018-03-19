package com.example.teodordimitrov.sampleapplication.providers;

import android.os.AsyncTask;
import android.util.Log;

import com.example.teodordimitrov.sampleapplication.application.BaseApplication;
import com.example.teodordimitrov.sampleapplication.beans.Instrument;
import com.example.teodordimitrov.sampleapplication.beans.InstrumentsResponse;
import com.example.teodordimitrov.sampleapplication.constants.Constants;
import com.example.teodordimitrov.sampleapplication.util.StringUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

/**
 * Prices provider class.
 * Will represent the actual data receiving.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class InstrumentProvider {

	public interface OnInstrumentsUpdatedListenerListener {
		void onUserInstrumentsUpdated (List<Instrument> instrumentsList);

		void onAllInstrumentsUpdated (List<Instrument> instrumentsList);
	}

	private static final String FILE_MOCKED_INSTRUMENTS_NAME = "mockedInstrumentPrices.json";

	private static final String INSTRUMENT_LOWEST_PRICE = "lowestPrice";
	private static final String INSTRUMENT_HIGHEST_PRICE = "highestPrice";

	@Inject
	protected Gson gson;

	private OnInstrumentsUpdatedListenerListener instrumentsListener;
	private UpdateInstrumentsAsyncTask instrumentsAsyncTask;

	public InstrumentProvider (OnInstrumentsUpdatedListenerListener instrumentsListener) {
		BaseApplication.getBaseComponent().inject(this);
		this.instrumentsListener = instrumentsListener;
	}

	/**
	 * Executes @{@link AsyncTask} to retrieve user's instruments' new prices.
	 * <p>
	 * <STRONG>The file is read every time.<STRONG>
	 * <STRONG>The purpose of it is to present a more accurate and time taking (not much, but still something) data receiving operation.</STRONG>
	 *
	 * @param userInstrumentsIds List of the ids of the user's instruments.
	 */
	public void updateInstrumentPrices (List<Long> userInstrumentsIds) {
		UpdateInstrumentsAsyncTask pricesAsyncTask = new UpdateInstrumentsAsyncTask(userInstrumentsIds, false);
		pricesAsyncTask.execute();
	}

	public void getAllInstruments () {
		UpdateInstrumentsAsyncTask instrumentsAsyncTask = new UpdateInstrumentsAsyncTask(new ArrayList<>(), true);
		instrumentsAsyncTask.execute();
	}

	private InstrumentsResponse getInstruments (JSONObject instrumentsJson, List<Long> userInstrumentsIds, boolean getAllInstruments) {
		JSONObject updatedInstrumentsJson = new JSONObject();
		JSONArray updatedInstrumentsJsonArray = new JSONArray();
		try {
			JSONArray instrumentsJsonArray = instrumentsJson.getJSONArray("instrumentsList");
			for (int i = 0; i < instrumentsJsonArray.length(); i++) {
				JSONObject instrumentJson = instrumentsJsonArray.getJSONObject(i);
				addNewInstrument(instrumentJson, getAllInstruments, userInstrumentsIds, updatedInstrumentsJsonArray);
			}
			updatedInstrumentsJson.put("instrumentsList", updatedInstrumentsJsonArray);
		} catch (JSONException e) {
			Log.e(Constants.TAG, "Exception occurred while parsing mocked json", e);
		}
		InstrumentsResponse instrumentsResponse = gson.fromJson(updatedInstrumentsJson.toString(), InstrumentsResponse.class);
		return instrumentsResponse;
	}

	/**
	 * @param getAllInstruments           based on this param, the array will contain all or only the users instruments.
	 * @param userInstrumentsIds          user's instruments' ids.
	 * @param updatedInstrumentsJsonArray the array containing all the instruments the user will need.
	 * @throws JSONException
	 */
	private void addNewInstrument (JSONObject instrumentJson, boolean getAllInstruments, List<Long> userInstrumentsIds, JSONArray updatedInstrumentsJsonArray) throws JSONException {
		long instrumentId = instrumentJson.getLong(Instrument.FIELD_NAME_ID);
		if (getAllInstruments) {
			updateInstrumentData(updatedInstrumentsJsonArray, instrumentJson);
		} else {
			if (userInstrumentsIds.contains(instrumentId)) {
				updateInstrumentData(updatedInstrumentsJsonArray, instrumentJson);
			}
		}
	}

	/**
	 * Updates mocked information data with random current prices.
	 *
	 * @param updatedInstrumentsJsonArray The json array where the new instruments will be hold.
	 * @param instrumentJson              The instrument json to be updated.
	 * @throws JSONException
	 */
	private void updateInstrumentData (JSONArray updatedInstrumentsJsonArray, JSONObject instrumentJson) throws JSONException {
		float lowestPrice = (float) instrumentJson.getDouble(INSTRUMENT_LOWEST_PRICE);
		float highestPrice = (float) instrumentJson.getDouble(INSTRUMENT_HIGHEST_PRICE);
		instrumentJson.remove(INSTRUMENT_LOWEST_PRICE);
		instrumentJson.remove(INSTRUMENT_HIGHEST_PRICE);
		instrumentJson.put(Instrument.FIELD_NAME_CURRENT_PRICE, generateRandomFloat(lowestPrice, highestPrice));
		updatedInstrumentsJsonArray.put(instrumentJson);
	}

	/**
	 * Reading from the file stored in assets folder.
	 * Representing mocked information.
	 * <p>
	 * <STRONG>The file is read every time.<STRONG>
	 * <STRONG>The purpose of it is to present a more accurate and time taking data receiving operation.</STRONG>
	 *
	 * @return String representation of the file.
	 */
	private String readMockedJsonFile () {
		String mockedJson = StringUtils.EMPTY;
		try {
			InputStream is = BaseApplication.getContext().getAssets().open(FILE_MOCKED_INSTRUMENTS_NAME);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			mockedJson = new String(buffer, "UTF-8");
		} catch (IOException e) {
			Log.e(Constants.TAG, "Exception occurred while reading from mocked json", e);
		}
		return mockedJson;
	}

	/**
	 * Generates random float between the bounds
	 * and makes it #.## format.
	 * #.## format is achieved from Math.round(randomFloatValue * 100) / 100
	 *
	 * @param lowerBound lower number
	 * @param upperBound higher number
	 * @return random number between lower and upper bounds in #.## format.
	 */
	private float generateRandomFloat (float lowerBound, float upperBound) {
		Random random = new Random();
		float randomFloatValue = lowerBound + random.nextFloat() * (upperBound - lowerBound);
		return (float) Math.round(randomFloatValue * 100) / 100;
	}

	public void setInstrumentListener (OnInstrumentsUpdatedListenerListener instrumentListener) {
		this.instrumentsListener = instrumentListener;
	}

	public void cancelPriceUpdate () {
		instrumentsAsyncTask.cancel(true);
	}

	private class UpdateInstrumentsAsyncTask extends AsyncTask<Void, String, InstrumentsResponse> {

		private List<Long> instrumentsIds;
		private boolean getAllInstruments;

		UpdateInstrumentsAsyncTask (List<Long> instrumentsIds, boolean getAllInstruments) {
			this.instrumentsIds = instrumentsIds;
			this.getAllInstruments = getAllInstruments;
		}

		@Override
		protected InstrumentsResponse doInBackground (Void... voids) {
			String instrumentsFileContent = readMockedJsonFile();
			JSONObject instrumentsJson = new JSONObject();
			try {
				instrumentsJson = new JSONObject(instrumentsFileContent);
			} catch (JSONException e) {
				Log.e(Constants.TAG, "Exception occurred while parsing mocked json", e);
			}
			return getInstruments(instrumentsJson, instrumentsIds, getAllInstruments);
		}

		@Override
		protected void onPostExecute (InstrumentsResponse instrumentsResponse) {
			if (isCancelled()) {
				return;
			}
			if (instrumentsResponse != null && instrumentsResponse.getInstrumentsList() != null) {
				if (getAllInstruments) {
					instrumentsListener.onAllInstrumentsUpdated(instrumentsResponse.getInstrumentsList());
				} else {
					instrumentsListener.onUserInstrumentsUpdated(instrumentsResponse.getInstrumentsList());
				}
			}
		}
	}

}
