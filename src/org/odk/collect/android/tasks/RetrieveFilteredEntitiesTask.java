package org.odk.collect.android.tasks;

import java.util.ArrayList;

import org.odk.collect.android.database.EntityIdAdapter;
import org.odk.collect.android.listeners.RetrieveEntityIdsListener;
import org.odk.collect.android.logic.CustomItem;

import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;

	/**
	 * Retrieve the results of Individuals based on what is set in the query fields
	 */
	public class RetrieveFilteredEntitiesTask extends AsyncTask<Void , Integer, ArrayList<CustomItem>> {
		
		private EntityIdAdapter entityIdAdapter;
		private RetrieveEntityIdsListener listener;
		
		private String[] dataParams;
		private String type;

		public RetrieveFilteredEntitiesTask(String []params, RetrieveEntityIdsListener listener) {
			this.dataParams = params;
			this.type = params[0];
			this.listener = listener;
		}
		
		protected ArrayList<CustomItem> doInBackground(Void ... params) {
	
			entityIdAdapter = new EntityIdAdapter();		
			entityIdAdapter.open();
						
			StringBuilder builder = new StringBuilder();	
			ArrayList<CustomItem> output = new ArrayList<CustomItem>();
			Cursor cursor = null;
			
			StringBuilder table = new StringBuilder("individual");
			
			if (type.equalsIgnoreCase("individual")) {
				if (!dataParams[1].equals(""))
					builder.append("firstname like '" + dataParams[1] + "' ");
				
				if (!dataParams[2].equals("")) {
					if (builder.length() > 0)
						builder.append("and ");
					builder.append("lastname like '" + dataParams[2] + "'");	
				}
				if (!dataParams[3].equals("")) {
					if (builder.length() > 0)
						builder.append("and " );
					builder.append("gender like '" + dataParams[3] + "'");
				}
				
				if (!TextUtils.isEmpty(dataParams[4])) {
					if (builder.length() > 0) {
						builder.append("and ");
					}
					table.append(" i inner join membership m on i._id = m._id");
					builder.append(" m." + EntityIdAdapter.KEY_SOCIALGROUP_EXT_ID + " like '" + dataParams[4] + "'");
				}
				
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query(table.toString(), new String [] {"extId", "firstname", "lastname"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("individual", new String [] {"extId", "firstname", "lastname"}, null, null, null, null, "extId");		
			
				cursor.moveToFirst();
				while (!cursor.isAfterLast() && !isCancelled()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String fname = cursor.getString(1);
					String lname = cursor.getString(2);
					item.setName(fname + " " + lname);
					
					output.add(item);	
					cursor.moveToNext();
				}
			}
			else if (type.equalsIgnoreCase("location")) {
				if (!dataParams[1].equals(""))
					builder.append("name like '" + dataParams[1] + "' ");
							
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query("location", new String [] {"extId", "name"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("location", new String [] {"extId", "name"}, null, null, null, null, "extId");
				
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String locName = cursor.getString(1);
					item.setName(locName);
					
					output.add(item);	
					cursor.moveToNext();
				}
			}
			
			else if (type.equalsIgnoreCase("village")) {
				if (!dataParams[1].equals(""))
					builder.append("name like '" + dataParams[1] + "' ");
							
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query("locationhierarchy", new String [] {"extId", "name"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("locationhierarchy", new String [] {"extId", "name"}, null, null, null, null, "extId");
				
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String villageName = cursor.getString(1);
					item.setName(villageName);
					
					output.add(item);	
					cursor.moveToNext();
				}
			}
			
			else if (type.equalsIgnoreCase("household")) {
				if (!dataParams[1].equals(""))
					builder.append("name like '" + dataParams[1] + "' ");
							
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query("household", new String [] {"extId", "name"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("household", new String [] {"extId", "name"}, null, null, null, null, "extId");

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String sgName = cursor.getString(1);
					item.setName(sgName);
					
					output.add(item);	
					cursor.moveToNext();
				}
			}
			else if (type.equalsIgnoreCase("visit")) {
				if (!dataParams[1].equals(""))
					builder.append("round like '" + dataParams[1] + "' ");
							
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query("visit", new String [] {"extId", "round"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("visit", new String [] {"extId", "round"}, null, null, null, null, "extId");

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String visitRound = cursor.getString(1);
					item.setName("Round: " + visitRound);
					
					output.add(item);	
					cursor.moveToNext();
				}
			} 
			else if (type.equalsIgnoreCase("fieldworker")) {
				if (!dataParams[1].equals(""))
					builder.append("firstname like '" + dataParams[1] + "' ");
				
				if (!dataParams[2].equals("")) {
					if (builder.length() > 0)
						builder.append("and ");
					builder.append("lastname like '" + dataParams[2] + "'");	
				}
				
				
				if (builder.length() > 0)
					cursor = entityIdAdapter.getmDb().query("fieldworker", new String [] {"extId", "firstname", "lastname"}, builder.toString(), null, null, null, "extId");
				else
					cursor = entityIdAdapter.getmDb().query("fieldworker", new String [] {"extId", "firstname", "lastname"}, null, null, null, null, "extId");		
			
				cursor.moveToFirst();
				while (!cursor.isAfterLast() && !isCancelled()) {
					CustomItem item = new CustomItem();
					String id = cursor.getString(0);
					item.setExtId(id);
						
					String fname = cursor.getString(1);
					String lname = cursor.getString(2);
					item.setName(fname + " " + lname);
					
					output.add(item);	
					cursor.moveToNext();
				}
			}

			cursor.close();
			entityIdAdapter.close();
			return output;
		}
				
	    protected final void onPostExecute(ArrayList<CustomItem> output) {
			listener.collectionComplete(output);
		}
	}