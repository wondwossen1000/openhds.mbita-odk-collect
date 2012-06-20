package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ValidationMessageActivity extends Activity {

	public static final String INSTANCE_PATH = null;
	private TextView formValidationMessageTextView;
	private TextView fieldValidationMessageTextView;
	private String instancePath;
	private final StringBuilder formMessageBuilder = new StringBuilder();
	private final Map<String, List<String>> fieldMessages = new HashMap<String, List<String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_validation_messages);
		
		formValidationMessageTextView = (TextView) findViewById(R.id.formValidationMessages);
		fieldValidationMessageTextView = (TextView) findViewById(R.id.fieldValidationMessages);
		
		instancePath = getIntent().getStringExtra(INSTANCE_PATH);
		
		fetchValidationMessagesFromDb();
		displayMessages();
	}

	private void fetchValidationMessagesFromDb() {
		FileDbAdapter db = new FileDbAdapter();
		db.open();
		
		Cursor c = db.fetchValidationMessages(instancePath);
		int elementIndex = c.getColumnIndex(FileDbAdapter.VALIDATION_KEY_MODEL_ELEMENT);
		int messageIndex = c.getColumnIndex(FileDbAdapter.VALIDATION_KEY_MESSAGE);
		
		if (c.moveToFirst()) {
			do {
				String element = c.getString(elementIndex);
				if (element == null) {
					addFormLevelFailureMessages(c.getString(messageIndex));
				} else {
					addFieldLevelValidationMessage(element, c.getString(messageIndex));
				}
			} while(c.moveToNext());
		}
		c.close();
		db.close();
	}

	private void addFormLevelFailureMessages(String string) {
		if (formMessageBuilder.length() > 0) {
			formMessageBuilder.append("\n");
		}
		
		formMessageBuilder.append(string);
	}

	private void addFieldLevelValidationMessage(String element, String message) {
		element = extractFieldNameFromXpathExpression(element);
		if (fieldMessages.containsKey(element)) {
			fieldMessages.get(element).add(message);
		} else {
			List<String> messages = new ArrayList<String>();
			messages.add(message);
			fieldMessages.put(element, messages);
		}
	}

	private String extractFieldNameFromXpathExpression(String element) {
		int index = element.lastIndexOf('/');
		return element.substring(index + 1);
	}

	private void displayMessages() {
		formValidationMessageTextView.setText(getFormValidationMessagesAsString());
		fieldValidationMessageTextView.setText(getFieldValidationMessagesAsString());
	}

	private String getFormValidationMessagesAsString() {
		if (formMessageBuilder.length() == 0) {
			return getString(R.string.validation_no_messsages) + "\n"; 
		}
		
		formMessageBuilder.append('\n');
		return formMessageBuilder.toString();
	}	
	
	private CharSequence getFieldValidationMessagesAsString() {
		StringBuilder builder = new StringBuilder();
		
		Set<Entry<String, List<String>>> entries = fieldMessages.entrySet();
		for(Entry<String, List<String>> entry : entries) {
			builder.append(entry.getKey() + ":\n");
			for(String message : entry.getValue()) {
				builder.append(message + "\n");
			}
		}
		
		if (builder.length() == 0) {
			return getString(R.string.validation_no_messsages);
		}
		
		return builder.toString();
	}
}
