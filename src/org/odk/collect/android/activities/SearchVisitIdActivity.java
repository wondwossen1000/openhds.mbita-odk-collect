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
 * Custom search Activity used for filtering the results of Visits.
 * Currently, it's possible to search only by the round number.
 */
public class SearchVisitIdActivity extends Activity {
	
	private TextView roundText;
	private Button clearButton;
	private Button searchButton;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search_visit_layout);
        
        roundText = (TextView)findViewById(R.id.roundText);
        searchButton = (Button)findViewById(R.id.filterResults);
        clearButton = (Button)findViewById(R.id.clear);
        
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	roundText.setText("");
            }
        });
        
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	Bundle bundle = new Bundle();
            	bundle.putString("type", "visit");
            	bundle.putString("round", roundText.getText().toString());
            	
            	Intent intent = new Intent();
            	intent.putExtras(bundle);        	
            	setResult(4, intent);
            	finish();
            }
        });
	}
}
