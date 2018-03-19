package com.example.teodordimitrov.sampleapplication.callbacks;

import com.example.teodordimitrov.sampleapplication.beans.Instrument;

/**
 * Used for callbacks between the adapter and the context
 * in which it is used.
 *
 * @author teodor.dimitrov on 19.3.2018 г..
 */

public interface OnInstrumentsRemovedListener {
	void onInstrumentsRemoved (Instrument instrument);
}
