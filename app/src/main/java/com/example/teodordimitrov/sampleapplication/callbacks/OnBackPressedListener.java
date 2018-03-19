package com.example.teodordimitrov.sampleapplication.callbacks;

/**
 * On back button pressed.
 * Used for callback from fragments to parent activity.
 *
 * @author teodor.dimitrov on 19.3.2018 г..
 */

public interface OnBackPressedListener {
	void onBack (boolean shouldLogout);
}