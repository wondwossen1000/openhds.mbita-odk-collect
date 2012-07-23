package org.odk.collect.android.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.odk.collect.android.database.EntityIdAdapter;
import org.odk.collect.android.listeners.CollectEntityIdsListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * An AsyncTask to upload all OpenHDS data from the web service configured in ServerPreferences.
 * This class has a filter: if true, only the hierarchy data will be uploaded.
 * If false, all core entities will be uploaded.
 */
public class SyncEntityIdsTask extends AsyncTask<Void, Integer, Boolean> {
       
    private CollectEntityIdsListener listener;
    private EntityIdAdapter entityIdAdapter;
    private String baseurl;
    private String username;
    private String password;
    private boolean filter;
    
    private ProgressDialog dialog;
   
    private UsernamePasswordCredentials creds;
    private HttpGet httpGet;
    private HttpClient client;
       
    private static final String ENTITY_INDIVIDUAL = "individual";
    private static final String ENTITY_LOCATION = "location";
    private static final String ENTITY_HOUSEHOLD = "household";
    private static final String ENTITY_VISIT = "visit";
    private static final String ENTITY_HIERARCHY = "hierarchy";
    private static final String ENTITY_FW = "fieldworker";
    private static final String ENTITY_LOCHIERARCHY = "locationhierarchy";
       
    public SyncEntityIdsTask(String url, String username, String password, boolean filter, ProgressDialog dialog, CollectEntityIdsListener listener) {
        this.baseurl = url;
        this.username = username;
        this.password = password;
        this.filter = filter;
        this.dialog = dialog;
        this.listener = listener;
    }

    protected Boolean doInBackground(Void... params) {
               
        entityIdAdapter = new EntityIdAdapter();         
        creds = new UsernamePasswordCredentials(username, password);
       
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 0);
        HttpConnectionParams.setSoTimeout(httpParameters, 0);
        client = new DefaultHttpClient(httpParameters);
        
        if (filter)
            httpGet = new HttpGet(baseurl + "/hierarchy");
        else
            httpGet = new HttpGet(baseurl);
         
        try {
            processResponse();
        } catch (Exception e) {
            return false;
        } 
        return true;
    }
    
    protected void onProgressUpdate(Integer... progress) {
    	dialog.incrementProgressBy(progress[0]);
    	if (dialog.getProgress() > dialog.getMax()) {
    		dialog.dismiss();
    		dialog.setProgress(0);
    		dialog.setMax(0);
    	}
    }
   
    private void processResponse() throws Exception {
    	InputStream inputStream = getResponse();
        if (inputStream != null)
            setupDB();

        processXMLDocument(inputStream);
    }
    
    private InputStream getResponse() throws AuthenticationException, ClientProtocolException, IOException {
        HttpResponse response = null;
        
        httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
        httpGet.addHeader("content-type", "application/xml");
        response = client.execute(httpGet);
       
        HttpEntity entity = response.getEntity();  
        return entity.getContent();
    }
   
    /**
     * Clear all tables
     */
    private void setupDB() {
        entityIdAdapter.open();
       
        if (filter)
            entityIdAdapter.getmDb().delete(ENTITY_HIERARCHY, null, null);
        else {
            entityIdAdapter.getmDb().delete(ENTITY_INDIVIDUAL, null, null);
            entityIdAdapter.getmDb().delete(ENTITY_FW, null, null);
            entityIdAdapter.getmDb().delete(ENTITY_LOCATION, null, null);
            entityIdAdapter.getmDb().delete(ENTITY_HOUSEHOLD, null, null);
            entityIdAdapter.getmDb().delete(ENTITY_VISIT, null, null);
            entityIdAdapter.getmDb().delete(ENTITY_LOCHIERARCHY, null, null);
        }
       
        entityIdAdapter.close();
    }
   
    /**
     * The response from the OpenHDS web service is an xml document
     * so it must be parsed.
     */
    private void processXMLDocument(InputStream content) throws Exception  {
       
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
       
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(content));
                      
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
            String name = null;
           
            switch(eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                                       
                    boolean isFinishedEntity = false;
                    
                    if (name.equalsIgnoreCase("count")) {
                    	parser.next();
                    	int count = Integer.parseInt(parser.getText());
                    	dialog.setMax(count);
                    	parser.nextTag();
                    }
   
                    else if (name.equalsIgnoreCase("entity")) {   
                   
                        Map<String, String> paramMap = new HashMap<String, String>();
                        String extId = null;
                       
                        while (!isFinishedEntity) {   
                            parser.nextTag();
                            name = parser.getName();
                           
                            if (name.equalsIgnoreCase("extId")) {
                                parser.next();
                                extId = parser.getText();
                                parser.nextTag();
                            }
                            else if (name.equalsIgnoreCase("params")) {
                               
                                boolean isFinishedParams = false;
                                while(!isFinishedParams) {
                                    parser.nextTag();
                                    name = parser.getName();
                                   
                                    if (name.equalsIgnoreCase("entry")) {
                                        parser.nextTag();
                                        parser.next();
                                        String key = parser.getText();
                                        parser.nextTag();
                                        parser.nextTag();
                                        parser.next();
                                        String value = parser.getText();
                                        parser.nextTag();
                                        parser.nextTag();
                                        paramMap.put(key, value);
                                    }
                                    else if (name.equalsIgnoreCase("params"))
                                        isFinishedParams = true;   
                                }
                            }
                            else if (name.equalsIgnoreCase("type")) {   
                                parser.next();
                                String type = parser.getText();
                               
                                if (type.equalsIgnoreCase("hierarchy"))
                                    saveHierarchyToDB(extId, paramMap.get("name"));
                                if (type.equalsIgnoreCase("village"))
                                	saveLocHierarchyToDB(extId, paramMap.get("name"));
                                if (type.equalsIgnoreCase("location"))
                                    saveLocationToDB(extId, paramMap.get("name"));
                                if (type.equalsIgnoreCase("individual"))
                                    saveIndividualToDB(extId, paramMap.get("firstname"), paramMap.get("lastname"), 
                                    		paramMap.get("gender"), paramMap.get("location"), paramMap.get("village"), getMemberships(paramMap));
                                if (type.equalsIgnoreCase("household"))
                                    saveHouseholdToDB(extId, paramMap.get("groupname"));
                                if (type.equalsIgnoreCase("visit"))
                                    saveVisitToDB(extId, paramMap.get("round"));
                                if (type.equalsIgnoreCase("fieldworker"))
                                    saveFieldworkerToDB(extId, paramMap.get("firstname"), paramMap.get("lastname"));
                               

                                publishProgress(1);
                                parser.nextTag();
                                isFinishedEntity = true;
                            }
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
    }
      
    private Set<String> getMemberships(Map<String, String> paramMap) {
    	Set<String> socialGroups = new HashSet<String>();
    	for(Entry<String, String> entry : paramMap.entrySet()) {
    		if (entry.getKey().startsWith("socialgroup")) {
    			socialGroups.add(entry.getValue());
    		}
    	}
    	return socialGroups;
    }

	public void saveHierarchyToDB(String extId, String name) {
         entityIdAdapter.open();
         entityIdAdapter.createHierarchy(extId, name);
         entityIdAdapter.close();
    }
    
  /*  public void saveLocHierarchyToDB(String extId, String name, String level) {
        entityIdAdapter.open();
        entityIdAdapter.createLocHierarchy(extId, name,level);
        entityIdAdapter.close();
   }*/
    public void saveLocHierarchyToDB(String extId, String name) {
        entityIdAdapter.open();
        entityIdAdapter.createLocHierarchy(extId, name);
        entityIdAdapter.close();
   }
    public void saveIndividualToDB(String extId, String firstname, String lastname, String gender, String location,String village, Set<String> memberships) {
         entityIdAdapter.open();
         entityIdAdapter.createIndividual(extId, firstname, lastname, gender, location,village, memberships);
         entityIdAdapter.close();
    }
   
    public void saveHouseholdToDB(String extId, String name) {
         entityIdAdapter.open();
         entityIdAdapter.createHousehold(extId, name);
         entityIdAdapter.close();
    }
   
    public void saveLocationToDB(String extId, String name) {
         entityIdAdapter.open();
         entityIdAdapter.createLocation(extId, name);
         entityIdAdapter.close();
    }
   
    public void saveVisitToDB(String extId, String round) {
         entityIdAdapter.open();
         entityIdAdapter.createVisit(extId, round);
         entityIdAdapter.close();
    }
   
    public void saveFieldworkerToDB(String extId, String firstname, String lastname) {
        entityIdAdapter.open();
        entityIdAdapter.createFieldworker(extId, firstname, lastname);
        entityIdAdapter.close();
   }
    
    protected void onPostExecute(final Boolean result) {
    	dialog.setProgress(0);
        listener.collectionComplete(result);
    }
}