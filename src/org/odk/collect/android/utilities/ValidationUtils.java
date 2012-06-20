package org.odk.collect.android.utilities;

import org.odk.collect.android.preferences.PreferencesActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ValidationUtils {

	public static boolean isValidationEnabled(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean(PreferencesActivity.VALIDATION_KEY_SERVER_ENABLED, false);
	}

}
