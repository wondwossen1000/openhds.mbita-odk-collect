package org.odk.collect.android.tasks;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.logic.ValidationProtocolImpl;
import org.odk.collect.android.logic.ValidationResult;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task responsible for fetching all completed forms, and uploading their
 * corresponding primary ids to check against any validation that has
 * occurred in Aggregate.
 * 
 * The protocol with Aggregate is as follows:
 * 1. Collect sends all ids of completed forms to Aggregate
 * 2. Aggregate will respond back with the same list of id's, as well
 * as indicating whether they've been validated. If they have failed
 * validation, a reason is supplied as well
 * 
 * The data representation is JSON
 */
public class ValidationTask extends AsyncTask<Void, Void, Void> {
	
	private static final int MAX_TIMEOUT = 10000;
	private final static String TAG = "ValidationTask";
	private final String serverUrl;
	private final ValidationTaskListener listener;
	private Outcome outcome;
	private final ValidationProtocolImpl impl;
	
	private enum Outcome {
		SUCCESS, 
		HTTP_SEND_FAILED, 
		HTTP_RESPONSE_FAIL,
		NO_COMPLETED_FORMS,
		BAD_JSON
	}
	
	public interface ValidationTaskListener {
		void onHttpSendFailure();
		
		void onHttpResponseFailure();
		
		void onSuccess();
		
		void onNoCompletedForms();
		
		void onCancel();
		
		void onBadJson();
	}
	
	public ValidationTask(String serverUrl, ValidationTaskListener listener) {
		this.serverUrl = serverUrl;
		this.listener = listener;
		this.impl = new ValidationProtocolImpl();
	}

	@Override
	protected void onPostExecute(Void arg) {
		switch (outcome) {
			case SUCCESS:
				listener.onSuccess();
				break;
			case HTTP_SEND_FAILED:
				listener.onHttpSendFailure();
				break;
			case HTTP_RESPONSE_FAIL:
				listener.onHttpResponseFailure();
				break;
			case NO_COMPLETED_FORMS:
				listener.onNoCompletedForms();
				break;
			case BAD_JSON:
				listener.onBadJson();
				break;
		}
	}	
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		listener.onCancel();
	}

	@Override
	protected Void doInBackground(Void... params) {
		JSONArray array = buildListOfSubmittedFormIdsAsJsonArray();

		if (array.length() == 0) {
			outcome = Outcome.NO_COMPLETED_FORMS;
			return null;
		}
		HttpResponse response = sendHttpRequest(array);
		
		if (isCancelled()) {
			return null;
		}
		
		if (response == null) {
			outcome = Outcome.HTTP_SEND_FAILED;
			return null;
		}
		
		handleResponse(response);
		return null;
	}

	private JSONArray buildListOfSubmittedFormIdsAsJsonArray() {
		/*FileDbAdapter db = new FileDbAdapter();
		db.open();
		Cursor cursor = db.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_SUBMITTED);*/
		
		// get all mInstances that match the status.
		String selection = InstanceColumns.STATUS + "=? or " + InstanceColumns.STATUS + "=?";
        String selectionArgs[] = {
                InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMITTED
        };

        Cursor c =  Collect.getInstance().getContentResolver()
        .query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
		//startManagingCursor(c);
		JSONArray array = impl.buildJsonArrayOfSubmittedFormIds(c);

		c.close();
		//db.close();
		return array;
	}
	
	private HttpResponse sendHttpRequest(JSONArray array) {
		HttpResponse response = null;
		
		try {
			String payload = array.toString();
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, MAX_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, MAX_TIMEOUT);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(serverUrl);
			post.setParams(params);
			post.addHeader("Content-Type", "application/json");
			HttpEntity entity = new StringEntity(payload);
			post.setEntity(entity);

			response =  client.execute(post);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem setting encoding on string entity");
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Http protocol error");
		} catch (IOException e) {
			Log.e(TAG, "IO Error with Http request");
		} catch(IllegalStateException e) {
			Log.e(TAG, "HttpClient not in a valid state, most likely the Url: " + serverUrl);
		}
		
		return response;
	}

	private void handleResponse(HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			processResponse(response.getEntity());
		} else {
			outcome = Outcome.HTTP_RESPONSE_FAIL;
		}
	}

	private void processResponse(HttpEntity entity) {
		try {
			String jsonObject = readResponseAsString(entity.getContent());
			List<ValidationResult> results = impl.parse(jsonObject);
			
			if (isCancelled()) {
				return;
			}
			
			writeResultsToDb(results);
			outcome = Outcome.SUCCESS;
		} catch (IllegalStateException e) {
			outcome = Outcome.HTTP_RESPONSE_FAIL;
		} catch (IOException e) {
			outcome = Outcome.HTTP_RESPONSE_FAIL;
		} catch (JSONException e) {
			outcome = Outcome.BAD_JSON;
		}
	}

	private String readResponseAsString(InputStream content) {
		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line = reader.readLine();
			
			while(line != null) {
				sb.append(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			Log.e(TAG, "Error reading the response content");
		}

		return sb.toString();
	}

	private void writeResultsToDb(List<ValidationResult> results) {
		FileDbAdapter db = new FileDbAdapter();
		db.open();
		
		db.deleteAllFromValidationTable();
		
		for(ValidationResult vr : results) {
			if (isCancelled()) {
				db.close();
				return;
			}
			
			db.createValidationMessages(vr.getFormInstanceId(), vr.getFormFailureMessages(), vr.getFieldFailureMessages());
		}
		
		db.close();
	}
}
