package com.example.teodordimitrov.sampleapplication.util;

import android.text.TextUtils;

/**
 * Util class for global validations.
 *
 * @author teodor.dimitrov on 17.3.2018 г..
 */

public class ValidationUtils {

	private static final int PASSWORD_MIN_CHARS = 5;

	public static boolean isEmailValid (CharSequence email) {
		return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	//TODO This should be a little bit more complex.
	public static boolean isPasswordValid (CharSequence password) {
		return password.length() > PASSWORD_MIN_CHARS;
	}
}
