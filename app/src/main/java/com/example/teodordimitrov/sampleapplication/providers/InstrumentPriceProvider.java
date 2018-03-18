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
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

/**
 * Prices provider class.
 * Will represent the actual data receiving.
 *
 * @author teodor.dimitrov on 18.3.2018 г..
 */

public class InstrumentPriceProvider {

	public interface OnPricesUpdatedListener {
		void onPricesUpdated (List<Instrument> instrumentsList);
	}

	private static final String FILE_MOCKED_INSTRUMENTS_NAME = "mockedInstrumentPrices.json";

	private static final String INSTRUMENT_LOWEST_PRICE = "lowestPrice";
	private static final String INSTRUMENT_HIGHEST_PRICE = "highestPrice";

	@Inject
	protected Gson gson;

	private OnPricesUpdatedListener pricesListener;
	private UpdatePricesAsyncTask pricesAsyncTask;

	public InstrumentPriceProvider (OnPricesUpdatedListener pricesListener) {
		BaseApplication.getBaseComponent().inject(this);
		this.pricesListener = pricesListener;
	}

	/**
	 * Executes @{@link AsyncTask} to retrieve user's instruments' new prices.
	 * <p>
	 * <STRONG>The file is read every time.<STRONG>
	 * <STRONG>The purpose of it is to present a more accurate and time taking (not much, but still something) data receiving operation.</STRONG>
	 *
	 * @param instrumentsIds List of the ids of the user's instruments.
	 */
	public void updatePrices (List<Long> instrumentsIds) {
		UpdatePricesAsyncTask pricesAsyncTask = new UpdatePricesAsyncTask(instrumentsIds);
		pricesAsyncTask.execute();
	}

	private InstrumentsResponse updatePrices (JSONObject instrumentsJson, List<Long> instrumentsIds) {
		JSONObject updatedInstrumentsJson = new JSONObject();
		JSONArray updateInstrumentsJsonArray = new JSONArray();
		try {
			JSONArray instrumentsJsonArray = instrumentsJson.getJSONArray("instrumentsList");
			for (int i = 0; i < instrumentsJsonArray.length(); i++) {
				JSONObject instrumentJson = instrumentsJsonArray.getJSONObject(i);
				long instrumentId = instrumentJson.getLong(Instrument.FIELD_NAME_ID);
				if (!instrumentsIds.contains(instrumentId)) {
					continue;
				}
				updateInstrumentData(updateInstrumentsJsonArray, instrumentJson);
			}
			updatedInstrumentsJson.put("instrumentsList", updateInstrumentsJsonArray);
		} catch (JSONException e) {
			Log.e(Constants.TAG, "Exception occurred while parsing mocked json", e);
		}
		InstrumentsResponse instrumentsResponse = gson.fromJson(updatedInstrumentsJson.toString(), InstrumentsResponse.class);
		return instrumentsResponse;
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

	private float generateRandomFloat (float lowerBound, float upperBound) {
		Random random = new Random();
		float randomFloatValue = lowerBound + random.nextFloat() * (upperBound - lowerBound);
		return randomFloatValue;
	}

	public void setPricesListener (OnPricesUpdatedListener pricesListener) {
		this.pricesListener = pricesListener;
	}

	public void cancelPriceUpdate () {
		pricesAsyncTask.cancel(true);
	}

	private class UpdatePricesAsyncTask extends AsyncTask<Void, String, InstrumentsResponse> {

		private List<Long> instrumentsIds;

		UpdatePricesAsyncTask (List<Long> instrumentsIds) {
			this.instrumentsIds = instrumentsIds;
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
			return updatePrices(instrumentsJson, instrumentsIds);
		}

		@Override
		protected void onPostExecute (InstrumentsResponse instrumentsResponse) {
			if (isCancelled()) {
				return;
			}
			if (instrumentsResponse != null && instrumentsResponse.getInstrumentsList() != null) {
				pricesListener.onPricesUpdated(instrumentsResponse.getInstrumentsList());
			}
		}
	}

}
