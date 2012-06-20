package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.CollectEntityIdsListener;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.SyncEntityIdsTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.AsyncTask.Status;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * An activity for collecting all of the OpenHDS target entity information
 */
public class CollectEntityIdActivity extends Activity implements CollectEntityIdsListener {
	
    private static final int ENTITY_ID_ACTIVITY = 1;
    private static final int MENU_LOOKUP_IDS = Menu.FIRST;
    
    private EditText editText;
    private Button mSyncEntityIdsButton;
    private ProgressDialog dialog;
    
    private SyncEntityIdsTask entityIdsTask = null;
    private SyncEntityIdsTask hierarchyIdsTask = null;
       
    private SharedPreferences settings;
    private String url;
    private String username;
    private String password;
    
    private WakeLock wl;
    
    private boolean isGeneratingHierarchy;
	
	public void onCreate(Bundle savedInstanceState) {
		  
		super.onCreate(savedInstanceState);
        setContentView(R.layout.collect_entity_id_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        
        editText = (EditText)findViewById(R.id.locationText);
        initializeProgressDialog();
             
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        url = settings.getString(PreferencesActivity.OPENHDS_KEY_SERVER, getString(R.string.default_openhdsserver));
        username = settings.getString(PreferencesActivity.OPENHDS_KEY_USERNAME, getString(R.string.username));
        password = settings.getString(PreferencesActivity.OPENHDS_KEY_PASSWORD, getString(R.string.password));
        
        mSyncEntityIdsButton = (Button) findViewById(R.id.sync_entityIds);
        mSyncEntityIdsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	isGeneratingHierarchy = false;
                if (entityIdsTask == null) {
                	dialog.show();
                	entityIdsTask = new SyncEntityIdsTask(url + "/entityIds/" + editText.getText(), username, password, false, dialog, CollectEntityIdActivity.this);
                }
                if (entityIdsTask.getStatus() == Status.PENDING) {  
                	entityIdsTask.execute();	
                }
            }
        });
                
        // collect hierarchy data first
        if (hierarchyIdsTask == null) {
        	dialog.show();
        	hierarchyIdsTask = new SyncEntityIdsTask(url, username, password, true, dialog, CollectEntityIdActivity.this);
        }	
        if (hierarchyIdsTask.getStatus() == Status.PENDING) {
        	isGeneratingHierarchy = true;
        	hierarchyIdsTask.execute();	
        }  
	}
	
	private void initializeProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Working...");
        dialog.setMessage("Do not interrupt");
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new MyOnCancelListener());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (hierarchyIdsTask != null)
			hierarchyIdsTask.cancel(true);
		if (entityIdsTask != null)
			entityIdsTask.cancel(true);
		wl.release();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
	}
	
	private class MyOnCancelListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface dialog) {
			if (hierarchyIdsTask != null)
				hierarchyIdsTask.cancel(true);
			if (entityIdsTask != null)
				entityIdsTask.cancel(true);
			finish();
			Toast.makeText(getApplicationContext(),	
					getString(R.string.sync_interrupted), 
					Toast.LENGTH_SHORT).show();
		}	
	}
	
	public void collectionComplete(Boolean result) {
		if (isGeneratingHierarchy) {
			if (result) {
				Toast.makeText(getApplicationContext(),	
						getString(R.string.sync_hierarchyIds_successful), 
						Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.sync_hierarchyIds_failure), 
						Toast.LENGTH_SHORT).show();
			}	
		}
		else {
			if (result) {
				Toast.makeText(getApplicationContext(),	
						getString(R.string.sync_entityIds_successful), 
						Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.sync_entityIds_failure), 
						Toast.LENGTH_SHORT).show();
			}	
		}
		dialog.dismiss();
		entityIdsTask = null;
		hierarchyIdsTask = null;
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(MENU_LOOKUP_IDS);    
        menu.add(0, MENU_LOOKUP_IDS, 0, R.string.lookupIds)
        	.setIcon(android.R.drawable.ic_menu_search)
        	.setEnabled(true);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LOOKUP_IDS:
            	Bundle bundle = new Bundle();
            	bundle.putString("hierarchy", "true");
            	Intent intent = new Intent(this, EntityIdActivity.class);
            	intent.putExtras(bundle);
                startActivityForResult(intent, ENTITY_ID_ACTIVITY);
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    	initializeProgressDialog();

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case ENTITY_ID_ACTIVITY:
            	editText.setText(intent.getAction());
            	break;
        }
    }
}
