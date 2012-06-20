package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Custom search Activity used for filtering the results of fieldworkers.
 * It's possible to search by first/last name as well as gender.
 */
public class SearchFieldWorkerIdActivity extends Activity {
	
	private TextView firstNameText;
	private TextView lastNameText;
	private Button searchButton;
	private Button clearButton;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search_fieldworker_layout);
        
        firstNameText = (TextView)findViewById(R.id.firstNameText);
        lastNameText = (TextView)findViewById(R.id.lastNameText);
        searchButton = (Button)findViewById(R.id.filterResults);
        clearButton = (Button)findViewById(R.id.clear);
        
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	firstNameText.setText("");
            	lastNameText.setText("");
            }
        });
        
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	Bundle bundle = new Bundle();
            	bundle.putString("type", "fieldworker");
            	bundle.putString("firstname", firstNameText.getText().toString());
            	bundle.putString("lastname", lastNameText.getText().toString());
            	
            
            	
            	Intent intent = new Intent();
            	intent.putExtras(bundle);        	
            	setResult(1, intent);
            	finish();
            }
        });
	}
}
