package com.example.teodordimitrov.sampleapplication.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.teodordimitrov.sampleapplication.constants.Constants;
import com.example.teodordimitrov.sampleapplication.util.StringUtils;

/**
 * Shared preferences manager used for abstraction over shared preferences.
 * The sample application uses shared preferences with encryption to store the email and password.
 * The sample application also uses shared preferences to save the remembered user favourite instruments.
 * Not the best practice but there is only one user remembered, and there is great encryption.
 * <p>
 * TODO if there is time left migrate to SQLLite or ROOM.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */

public class SharedPreferencesManager {

	private static final String SHARED_PREFERENCES_ENCRYPTED = "com.example.teodordimitrov.sampleapplication.encrypted";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_PASSWORD = "credential";
	private static final String KEY_ENCRYPTED_SEQUENCE = "encryptedKey";

	private SharedPreferences credentialsSharedPreferences;

	private SecurityManager securityManager;

	public SharedPreferencesManager (Context context) {
		securityManager = new SecurityManager(context, this);
		credentialsSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_ENCRYPTED, Context.MODE_PRIVATE);
	}

	public void saveUserCredentials (String email, String password) {
		SharedPreferences.Editor editor = credentialsSharedPreferences.edit();
		try {
			editor.putString(KEY_EMAIL, securityManager.encryptData(email))
					.putString(KEY_PASSWORD, securityManager.encryptData(password)).apply();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Exception occurred while storing credentials ", e);
		}
	}

	public String getStoredEmail () {
		return decryptCredential(KEY_EMAIL);
	}

	public String getStoredPassword () {
		return decryptCredential(KEY_PASSWORD);
	}

	String getEncryptedKey () {
		return credentialsSharedPreferences.getString(KEY_ENCRYPTED_SEQUENCE, null);
	}

	void saveEncryptedKey (String encryptedKeyBase64encoded) {
		credentialsSharedPreferences.edit()
				.putString(KEY_ENCRYPTED_SEQUENCE, encryptedKeyBase64encoded)
				.apply();
	}

	void removeEncryptedKey () {
		credentialsSharedPreferences.edit().remove(KEY_ENCRYPTED_SEQUENCE).apply();
	}

	private String decryptCredential (String key) {
		String decryptedCredential = credentialsSharedPreferences.getString(key, StringUtils.EMPTY);
		if (!decryptedCredential.isEmpty()) {
			try {
				decryptedCredential = securityManager.decryptData(decryptedCredential);
			} catch (Exception e) {
				Log.e(Constants.TAG, "Exception occurred while recovering credentials ", e);
			}
		}
		return decryptedCredential;
	}

	public void clearCredentials () {
		credentialsSharedPreferences.edit().clear().apply();
	}
}
