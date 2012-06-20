package org.odk.collect.android.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.odk.collect.android.database.FileDbAdapter;

import android.database.Cursor;

/**
 * Implements the protocol for communicating with the validation server
 */
public class ValidationProtocolImpl {

	private static final String FORM_FAILURE_MESSAGES_PROPERTY = "formFailureMessages";
	private static final String FIELD_FAILURE_MESSAGES_PROPERTY = "fieldFailureMessages";
	private static final String FIELD_FAILURE_REASONS_PROPERTY = "failureReasons";
	private static final String ID_PROPERTY = "id";
	private static final String MODEL_ELEMENT_PROPERTY = "modelElement";
	private static final String VALID_PROPERTY = "valid";
	private static final String PROCESSED_PROPERTY = "processed";	
	
	private static final String OUTER_ELEMENT_EXCEPTION = "Expected an array as the outer most element.";

	private JSONArray outerArray;
	
	public JSONArray buildJsonArrayOfSubmittedFormIds(Cursor cursor) {
		JSONArray array = new JSONArray();
		
		if (cursor.moveToFirst()) {
			do {
				int columnIndex = cursor.getColumnIndex(FileDbAdapter.KEY_INSTANCE_ID);
				array.put(cursor.getString(columnIndex));
			} while(cursor.moveToNext());
		}
		
		return array;
	}
	
	public List<ValidationResult> parse(String jsonPayload) throws JSONException {
		convertStringToJson(jsonPayload);
		return iterateArrayElementsForResults();
	}

	private void convertStringToJson(String jsonPayload) throws JSONException {
		JSONTokener tokener = new JSONTokener(jsonPayload);
		Object val = tokener.nextValue();
		
		if (val instanceof JSONArray) {
			outerArray = (JSONArray) val;
		} else {
			throw new JSONException(OUTER_ELEMENT_EXCEPTION);
		}
	}

	private List<ValidationResult> iterateArrayElementsForResults() throws JSONException {
		List<ValidationResult> results = new ArrayList<ValidationResult>();
		
		for(int i = 0; i < outerArray.length(); i++) {
			JSONObject jo = outerArray.getJSONObject(i);
			if (jo.getBoolean(PROCESSED_PROPERTY) && !jo.getBoolean(VALID_PROPERTY)) {
				ValidationResult res = createValidationResult(jo);
				results.add(res);
			}
		}
		
		return results;
	}
	
	private ValidationResult createValidationResult(JSONObject jo) throws JSONException {
		List<String> formFailureMessages = processFormFailureMessages(jo.getJSONArray(FORM_FAILURE_MESSAGES_PROPERTY));
		Map<String, String[]> fieldFailureMessages = processFieldFailureMesssages(jo.getJSONArray(FIELD_FAILURE_MESSAGES_PROPERTY));
		
		return new ValidationResult(jo.getString(ID_PROPERTY), formFailureMessages, fieldFailureMessages);
	}

	private List<String> processFormFailureMessages(JSONArray formFailureMessages) throws JSONException {
		List<String> convertedFormFailureMessages = new ArrayList<String>();
		
		if (formFailureMessages.length() > 0) {
			for(int i = 0; i < formFailureMessages.length(); i++) {
				convertedFormFailureMessages.add(formFailureMessages.getString(i));
			}
		}

		return convertedFormFailureMessages;

	}

	private Map<String, String[]> processFieldFailureMesssages(JSONArray array) throws JSONException {
		Map<String, String[]> fieldFailureMessages = new HashMap<String, String[]>();
		
		for(int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String modelElement = obj.getString(MODEL_ELEMENT_PROPERTY);
						
			JSONArray failMessages = obj.getJSONArray(FIELD_FAILURE_REASONS_PROPERTY);
			String[] messages = new String[failMessages.length()];
		
			for(int j = 0; j < failMessages.length(); j++) {
				messages[j] = failMessages.getString(j);
			}
			
			fieldFailureMessages.put(modelElement, messages);
		}
		
		return fieldFailureMessages;
	}

}
