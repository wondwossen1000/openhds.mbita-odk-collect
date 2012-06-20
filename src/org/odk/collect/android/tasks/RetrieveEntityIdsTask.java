package org.odk.collect.android.tasks;

import java.util.ArrayList;
import org.odk.collect.android.database.EntityIdAdapter;
import org.odk.collect.android.listeners.RetrieveEntityIdsListener;
import org.odk.collect.android.logic.CustomItem;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;

	/**
	 * AsyncTask that queries the database and returns a list of results based on
	 * what is selected in the spinner. 
	 */
    public class RetrieveEntityIdsTask extends AsyncTask<Void, Integer, ArrayList<CustomItem>> {

    	private RetrieveEntityIdsListener listener;
    	private EntityIdAdapter entityIdAdapter;
		private String param;
		private boolean isHierarchy;
		private boolean isIndividual;
		private boolean isLocation;
		private boolean isHousehold;
		private boolean isVisit;
		private boolean isFW;
		private boolean isVillage;
		
		private ProgressDialog dialog;
		
		public RetrieveEntityIdsTask(String entityName, ProgressDialog dialog, RetrieveEntityIdsListener listener) {
			this.param = entityName;
			this.dialog = dialog;
			this.listener = listener;
		}
			
		protected ArrayList<CustomItem> doInBackground(Void... params) {
						
			entityIdAdapter = new EntityIdAdapter();		
			entityIdAdapter.open();
			
			ArrayList<CustomItem> output = new ArrayList<CustomItem>();
			Cursor cursor = null;
			
			if (param.equalsIgnoreCase("hierarchy")) {
				isHierarchy = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "name"}, null, null, null, null, "extId");
			}
			else if (param.equalsIgnoreCase("individual")) {
				isIndividual = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "firstname", "lastname"}, null, null, null, null, "extId");
			}
			else if (param.equalsIgnoreCase("location")) {
				isLocation = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "name"}, null, null, null, null, "extId");
			}
			else if (param.equalsIgnoreCase("household")) {
				isHousehold = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "name"}, null, null, null, null, "extId");
			}
			else if (param.equalsIgnoreCase("visit")) {
				isVisit = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "round"}, null, null, null, null, "extId");
			}else if (param.equalsIgnoreCase("fieldworker")) {
				isFW = true;
				cursor = entityIdAdapter.getmDb().query(param.toLowerCase(), new String [] {"extId", "firstname", "lastname"}, null, null, null, null, "extId");
			}else if (param.equalsIgnoreCase("village")) {
				isVillage = true;
				//cursor = entityIdAdapter.getmDb().query("locationhierarchy", new String [] {"extId", "name"}, "level_uuid ='ff80818134cdc6480134cdc8d36f0001'", null, null, null, "extId");
				cursor = entityIdAdapter.getmDb().query("locationhierarchy", new String [] {"extId", "name"}, null, null, null, null, "extId");
			}
			
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && !isCancelled()) {
				
				CustomItem item = new CustomItem();
				String id = cursor.getString(0);
				item.setExtId(id);
				
				if (isHierarchy) {
					String name = cursor.getString(1);
					item.setName(name);
				}
				else if (isIndividual) {
					String firstname = cursor.getString(1);
					String lastname = cursor.getString(2);
					item.setName(firstname + " " + lastname);
				}
				else if (isLocation) {
					String name = cursor.getString(1);
					item.setName(name);
				}
				else if (isHousehold) {
					String name = cursor.getString(1);
					item.setName(name);
				}
				else if (isVisit) {
					String round = cursor.getString(1);
					item.setName("Round: " + round);
				}
				else if (isFW) {
					String firstname = cursor.getString(1);
					String lastname = cursor.getString(2);
					item.setName(firstname + " " + lastname);
				}
				else if (isVillage) {
					String name = cursor.getString(1);
					item.setName(name);
				}
				publishProgress(1);
				output.add(item);	
				cursor.moveToNext();
			}
			cursor.close();
			entityIdAdapter.close();
			return output;
		}
		
	    protected void onProgressUpdate(Integer... progress) {
	    	dialog.incrementProgressBy(progress[0]);
	    	if (dialog.getProgress() > dialog.getMax()) {
	    		dialog.dismiss();
	    		dialog.setProgress(0);
	    		dialog.setMax(0);
	    	}
	    }

		protected final void onPostExecute(ArrayList<CustomItem> output) {
			listener.collectionComplete(output);
		}
    }