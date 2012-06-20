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
 * Custom search Activity used for filtering the results of Social Groups.
 * Currently, it's possible to search only by name.
 */
public class SearchHouseholdIdActivity extends Activity {
	
	private TextView nameText;
	private Button clearButton;
	private Button searchButton;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search_household_layout);
        
        nameText = (TextView)findViewById(R.id.nameText);
        searchButton = (Button)findViewById(R.id.filterResults);
        clearButton = (Button)findViewById(R.id.clear);
        
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	nameText.setText("");
            }
        });
        
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	Bundle bundle = new Bundle();
            	bundle.putString("type", "household");
            	bundle.putString("name", nameText.getText().toString());
            	
            	Intent intent = new Intent();
            	intent.putExtras(bundle);        	
            	setResult(3, intent);
            	finish();
            }
        });
	}
}
