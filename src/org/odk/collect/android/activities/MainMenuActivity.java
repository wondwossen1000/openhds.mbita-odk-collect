/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.ValidationTask;
import org.odk.collect.android.tasks.ValidationTask.ValidationTaskListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
//import android.app.SearchManager.OnDismissListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;



/**
 * Responsible for displaying buttons to launch the major activities. Launches some activities based
 * on returns of others.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity implements ValidationTaskListener {
    private static final String t = "MainMenuActivity";


// request codes for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;
    private static final int INSTANCE_CHOOSER = 1;
    private static final int INSTANCE_UPLOADER = 2;


    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_COLLECT_OPENHDS_IDS = 2;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;
    private Button mGetFormsButton;
 private Button mValidateFormButton;
    private AlertDialog mAlertDialog;

    private static boolean EXIT = true;
  private static final int VALIDATION_PROGRESS_DIALOG = 1;
	private ValidationTask validationAsyncTask;

    // private static boolean DO_NOT_EXIT = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an external intent
        Log.i(t, "Starting up, creating directories");
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        setContentView(R.layout.main_menu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setText(getString(R.string.enter_data_button));
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormChooserList.class);
                startActivity(i);
            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setText(getString(R.string.review_data_button));
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                startActivity(i);
            }
        });

        
        // validation button
        mValidateFormButton = (Button) findViewById(R.id.validate_forms);
        mValidateFormButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				attemptToExecuteValidationTask();
			}
        });
        
        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setText(getString(R.string.send_data_button));
        mSendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceUploaderList.class);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        mGetFormsButton = (Button) findViewById(R.id.get_forms);
        mGetFormsButton.setText(getString(R.string.get_forms));
        mGetFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormDownloadList.class);
                startActivity(i);

            }
        });

        // manage forms button. no result expected.
        mManageFilesButton = (Button) findViewById(R.id.manage_forms);
        mManageFilesButton.setText(getString(R.string.manage_files));
        mManageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FileManagerTabs.class);
                startActivity(i);
            }
        });
    }


private void attemptToExecuteValidationTask() {
		String validationServerUrl = getValidationServerUrl();

		if (validationServerUrl.equals("")) {
    		Toast.makeText(getApplicationContext(), R.string.validation_server_required, Toast.LENGTH_LONG).show();
    		return;
		}
		
		executeValidationTask(validationServerUrl);
	}

	protected String getValidationServerUrl() {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	String serverUrl = sp.getString(PreferencesActivity.VALIDATION_KEY_SERVER, "");

    	return serverUrl;
    }	
    
     private void executeValidationTask(String validationServerUrl) {
    	showDialog(VALIDATION_PROGRESS_DIALOG);
    	validationAsyncTask = new ValidationTask(validationServerUrl, MainMenuActivity.this);
   		validationAsyncTask.execute();		
	}


    @Override
    protected void onPause() {
        super.onPause();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

 private void setValidationButtonVisibility() {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	boolean visible = settings.getBoolean(PreferencesActivity.VALIDATION_KEY_SERVER_ENABLED, false);
    	int vis = (visible ? View.VISIBLE : View.GONE);
    	mValidateFormButton.setVisibility(vis);
    	findViewById(R.id.textBuffer).setVisibility(vis);
	}

	/*private void createPreferencesMenu() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }*/

	private void createCollectEntityActivity() {
     	Intent i = new Intent(getApplicationContext(), CollectEntityIdActivity.class);
     	startActivity(i);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.general_preferences)).setIcon(
            android.R.drawable.ic_menu_preferences);
        menu.add(1, MENU_COLLECT_OPENHDS_IDS, 0, getString(R.string.sync_entityIds)).setIcon(
        		android.R.drawable.ic_menu_upload);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent ig = new Intent(this, PreferencesActivity.class);
                startActivity(ig);
                return true;
            case MENU_COLLECT_OPENHDS_IDS:
            	createCollectEntityActivity();
            	return true;	
        }
        return super.onOptionsItemSelected(item);
    }


    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case VALIDATION_PROGRESS_DIALOG:
				ProgressDialog pd = new ProgressDialog(this);
				pd.setTitle(R.string.please_wait);
				pd.setMessage(getString(R.string.working_dialog_message));
				pd.setOnDismissListener(new DismissListener());
				return pd;
		}
		
		return super.onCreateDialog(id);
	}


@Override
public void onHttpSendFailure() {
	// TODO Auto-generated method stub
	
}


@Override
public void onHttpResponseFailure() {
	showToastMessage(R.string.validation_response_failure, Toast.LENGTH_SHORT);
	dismissDialog(VALIDATION_PROGRESS_DIALOG);
}


@Override
public void onSuccess() {
	showToastMessage(R.string.validation_success, Toast.LENGTH_SHORT);
	dismissDialog(VALIDATION_PROGRESS_DIALOG);
	//updateButtons();
}

@Override
public void onNoCompletedForms() {
	showToastMessage(R.string.validation_no_submitted_forms, Toast.LENGTH_SHORT);
	dismissDialog(VALIDATION_PROGRESS_DIALOG);
}

@Override
public void onCancel() {
	showToastMessage(R.string.validation_not_successful, Toast.LENGTH_SHORT);
	dismissDialog(VALIDATION_PROGRESS_DIALOG);
}


@Override
public void onBadJson() {
	showToastMessage(R.string.validation_bad_server_json, Toast.LENGTH_SHORT);
	dismissDialog(VALIDATION_PROGRESS_DIALOG);
}

private void showToastMessage(int resId, int duration) {
	Toast.makeText(this, resId, duration).show();
}

private class DismissListener implements OnDismissListener {

	@Override
	public void onDismiss(DialogInterface arg0) {
		validationAsyncTask.cancel(false);
	}
	
}
}
