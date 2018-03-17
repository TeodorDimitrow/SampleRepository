package com.example.teodordimitrov.sampleapplication.managers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Shared preferences manager used for abstraction over shared preferences.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */

public class SharedPreferencesManager {

	private static final String SHARED_PREFERENCES_NAME = "com.example.teodordimitrov.sampleapplication.preferences";

	private SharedPreferences sharedPreferences;

	public SharedPreferencesManager (Context context) {
		sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
}
