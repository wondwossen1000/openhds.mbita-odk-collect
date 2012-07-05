package org.odk.collect.android.activities;

import java.util.ArrayList;
import org.odk.collect.android.R;
import org.odk.collect.android.database.EntityIdAdapter;
import org.odk.collect.android.listeners.RetrieveEntityIdsListener;
import org.odk.collect.android.logic.CustomItem;
import org.odk.collect.android.tasks.RetrieveEntityIdsTask;
import org.odk.collect.android.tasks.RetrieveFilteredEntitiesTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * An activity for displaying the results of a selected entity type.
 * It handles all logic related to querying and displaying any of the
 * OpenHDS entity types.
 */
public class EntityIdActivity extends Activity implements RetrieveEntityIdsListener {
	
    public static final int SEARCH_INDIVIDUAL_ACTIVITY = 1;
    public static final int SEARCH_LOCATION_ACTIVITY = 2;
    public static final int SEARCH_HOUSEHOLD_ACTIVITY = 3;
    public static final int SEARCH_VISIT_ACTIVITY = 4;
    public static final int SEARCH_FIELDWORKER = 5;
    public static final int SEARCH_VILLAGE = 6;
    
    
    
	private ListView listView;
	private Spinner spinner;
	private Button filterButton;
    private ProgressDialog dialog;
    private ProgressDialog spinnerDialog;
    
    WakeLock wl;
    
    RetrieveEntityIdsTask retrieveEntityIdsTask = null;
    RetrieveFilteredEntitiesTask retrieveFilteredEntitiesTask = null;
       	
	public void onCreate(Bundle savedInstanceState) {
		  
		super.onCreate(savedInstanceState);
        setContentView(R.layout.entity_id_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        
        spinnerDialog = new ProgressDialog(this);
        spinnerDialog.setTitle("Working...");
        spinnerDialog.setMessage("Do not interrupt");
        
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Working...");
        dialog.setMessage("Do not interrupt");
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new MyOnCancelListener());
        
        spinner = (Spinner) findViewById(R.id.spinner);
        listView = (ListView)findViewById(R.id.listView);
        filterButton = (Button)findViewById(R.id.filterResults);
        filterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {  
            	if (spinner.getSelectedItem().toString().equalsIgnoreCase("individual")) {
            		Intent intent = new Intent(getApplicationContext(), SearchIndividualIdActivity.class);
                	startActivityForResult(intent, SEARCH_INDIVIDUAL_ACTIVITY);
            	}
            	else if (spinner.getSelectedItem().toString().equalsIgnoreCase("location")) {
	            	Intent intent = new Intent(getApplicationContext(), SearchLocationIdActivity.class);
                	startActivityForResult(intent, SEARCH_LOCATION_ACTIVITY);
            	}
            	else if (spinner.getSelectedItem().toString().equalsIgnoreCase("household")) {
	            	Intent intent = new Intent(getApplicationContext(), SearchHouseholdIdActivity.class);
                	startActivityForResult(intent, SEARCH_HOUSEHOLD_ACTIVITY);
            	}
            	else if (spinner.getSelectedItem().toString().equalsIgnoreCase("visit")) {
	            	Intent intent = new Intent(getApplicationContext(), SearchVisitIdActivity.class);
                	startActivityForResult(intent, SEARCH_VISIT_ACTIVITY);
            	}
            	else if (spinner.getSelectedItem().toString().equalsIgnoreCase("fieldworker")) {
	            	Intent intent = new Intent(getApplicationContext(), SearchFieldWorkerIdActivity.class);
                	startActivityForResult(intent, SEARCH_FIELDWORKER);
            	}
            	else if (spinner.getSelectedItem().toString().equalsIgnoreCase("village")) {
	            	Intent intent = new Intent(getApplicationContext(), SearchVillageIdActivity.class);
                	startActivityForResult(intent, SEARCH_VILLAGE);
            	}
            }
        });
        
        // used to filter what's displayed in the spinner list of choices 
        Bundle bundle = this.getIntent().getExtras();
        
        if (bundle != null) {
        	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            		this, R.array.hierarchy_array, android.R.layout.simple_spinner_item);
        	   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
               spinner.setAdapter(adapter);
        }
        else {
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        		this, R.array.entity_array, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);
        }
        
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        listView.setOnItemClickListener(new MyOnItemClickSelectedListener());
    }
		
	@Override
	public void collectionComplete(ArrayList<CustomItem> list) {
		
		if (list.size() == 0) {
			String [] array = {"No items found"};
			listView.setAdapter(new ArrayAdapter<String>(EntityIdActivity.this, android.R.layout.simple_list_item_1, array));
			filterButton.setVisibility(View.INVISIBLE);
		}
		else {	
			MyListAdapter adapter = new MyListAdapter(EntityIdActivity.this, R.layout.custom_row, list);
			listView.setAdapter(adapter);
			if (!spinner.getSelectedItem().toString().equalsIgnoreCase("hierarchy"))
				filterButton.setVisibility(View.VISIBLE);
		}
		dialog.dismiss();
		spinnerDialog.dismiss();
		retrieveEntityIdsTask = null;
		retrieveFilteredEntitiesTask = null;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (retrieveEntityIdsTask != null)
			retrieveEntityIdsTask.cancel(true);
		if (retrieveFilteredEntitiesTask != null)
			retrieveFilteredEntitiesTask.cancel(true);
		wl.release();
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
        	// request was canceled, so do nothing
            return;
        }
        
        Bundle bundle = intent.getExtras();
        String type = bundle.get("type").toString();
        switch (requestCode) {
        	case SEARCH_INDIVIDUAL_ACTIVITY:
            	String firstname = bundle.get("firstname").toString();
            	String lastname = bundle.get("lastname").toString();
            	String gender = bundle.get("gender").toString();
            	String household = bundle.getString("household");
            	 
            	String[] indivData = {type, firstname, lastname, gender, household};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(indivData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;
        	case SEARCH_LOCATION_ACTIVITY:
            	String locName = bundle.get("name").toString();
            	 
            	String[] locData = {type, locName};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(locData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;
           	case SEARCH_VILLAGE:
            	String vilName = bundle.get("name").toString();
            	 
            	String[] vilData = {type, vilName};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(vilData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;
        	case SEARCH_HOUSEHOLD_ACTIVITY:
            	String sgName = bundle.get("name").toString();
            	 
            	String[] sgData = {type, sgName};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(sgData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;
        	case SEARCH_VISIT_ACTIVITY:
            	String round = bundle.get("round").toString();
            	 
            	String[] visitData = {type, round};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(visitData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;	
        	case SEARCH_FIELDWORKER:
        		String fwfirstname = bundle.get("firstname").toString();
            	String fwlastname = bundle.get("lastname").toString();
            	
            	 
            	String[] fwindivData = {type, fwfirstname, fwlastname};
            	spinnerDialog.show();
            	retrieveFilteredEntitiesTask = new RetrieveFilteredEntitiesTask(fwindivData, EntityIdActivity.this); 
            	retrieveFilteredEntitiesTask.execute();
            	break;
        }
    }
		
	private void initializeProgressBar() {
		EntityIdAdapter entityIdAdapter = new EntityIdAdapter();
		String [] array = {};
		entityIdAdapter.open();
		SQLiteStatement	s;
		if (spinner.getSelectedItem().toString().equalsIgnoreCase("village")){
			s = entityIdAdapter.getmDb().compileStatement("select count(*) from locationhierarchy");
		} else { 
			s = entityIdAdapter.getmDb().compileStatement("select count(*) from " + spinner.getSelectedItem().toString());
		}
		int count = (int) s.simpleQueryForLong();
		
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Working...");
        dialog.setMessage("Do not interrupt");
        dialog.setCancelable(true);
		dialog.setProgress(0);
		dialog.setMax(count);
	
		listView.setAdapter(new ArrayAdapter<String>(EntityIdActivity.this, android.R.layout.simple_list_item_1, array));
		entityIdAdapter.close();
	}
	
	private class MyOnCancelListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface dialog) {
			finish();
			Toast.makeText(getApplicationContext(),	
					getString(R.string.sync_interrupted), 
					Toast.LENGTH_SHORT).show();
		}	
	}
	
	private class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			dialog.show();
	        initializeProgressBar();
	        retrieveEntityIdsTask = new RetrieveEntityIdsTask(parent.getItemAtPosition(pos).toString(), dialog, EntityIdActivity.this);
	        retrieveEntityIdsTask.execute();
		}

		@SuppressWarnings("rawtypes")
		public void onNothingSelected(AdapterView parent) { }
	}
	
	private class MyOnItemClickSelectedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Object o = parent.getItemAtPosition(position);
			String selectedId = null;
			
			if (o instanceof CustomItem) {
				CustomItem item = (CustomItem)o;
				selectedId = item.getExtId();
			}
			
			Intent resultIntent = new Intent(selectedId);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
	}
	
    /**
     * Custom Adapter for selecting a custom row layout
     */
    private class MyListAdapter extends ArrayAdapter<CustomItem> {
    	
    	private ArrayList<CustomItem> items;

		public MyListAdapter(Context context, int textViewResourceId, ArrayList<CustomItem> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
		}
    	
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.custom_row, null);
			}
			
			CustomItem item = items.get(position);
			if (item != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				
				if (tt != null) 
					tt.setText(item.getExtId());
				if (bt != null)
					bt.setText(item.getName());
			}
		    return v;
		}
    }
}
