package org.odk.collect.android.logic;

import java.util.List;
import java.util.Map;

public class ValidationResult {
	private final String formInstanceId;
	private final List<String> formFailureMessages;
	private final Map<String, String[]> fieldFailureMessages;
	
	public ValidationResult(String formInstanceId, List<String> formFailureMessages, Map<String, String[]> failureMessages) {
		this.formInstanceId = formInstanceId;
		this.formFailureMessages = formFailureMessages;
		this.fieldFailureMessages = failureMessages;
	}
	
	public String getFormInstanceId() {
		return formInstanceId;
	}
	
	public List<String> getFormFailureMessages() {
		return formFailureMessages;
	}
	
	public Map<String, String[]> getFieldFailureMessages() {
		return fieldFailureMessages;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(formInstanceId);
		builder.append(": ");
		
		for(Map.Entry<String, String[]> entry : fieldFailureMessages.entrySet()) {
			builder.append("[");
			builder.append(entry.getKey());
			builder.append(": ");
			builder.append(entry.getValue().toString());
			builder.append("]");
		}
		
		return builder.toString();
	}
}
