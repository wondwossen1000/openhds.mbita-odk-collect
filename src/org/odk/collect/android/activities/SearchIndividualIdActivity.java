package org.odk.collect.android.activities;

import org.odk.collect.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Custom search Activity used for filtering the results of Individuals.
 * It's possible to search by first/last name as well as gender.
 */
public class SearchIndividualIdActivity extends Activity {
	
	private TextView firstNameText;
	private TextView lastNameText;
	private RadioButton genderMaleRadioButton;
	private RadioButton genderFemaleRadioButton;
	private Button searchButton;
	private Button clearButton;
	private TextView householdLocationText;
	private Spinner householdLocationSpinner;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search_individual_layout);
        
        firstNameText = (TextView)findViewById(R.id.firstNameText);
        lastNameText = (TextView)findViewById(R.id.lastNameText);
        genderMaleRadioButton = (RadioButton)findViewById(R.id.male);
        genderFemaleRadioButton = (RadioButton)findViewById(R.id.female);
        searchButton = (Button)findViewById(R.id.filterResults);
        clearButton = (Button)findViewById(R.id.clear);
        householdLocationText = (TextView)findViewById(R.id.householdLocationText);
        
        householdLocationSpinner = (Spinner) findViewById(R.id.householdLocationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.entity_filter, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        householdLocationSpinner.setAdapter(adapter);
        
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	firstNameText.setText("");
            	lastNameText.setText("");
            	genderMaleRadioButton.setChecked(false);
            	genderFemaleRadioButton.setChecked(false);
            	householdLocationText.setText("");
            }
        });
        
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	Bundle bundle = new Bundle();
            	bundle.putString("type", "individual");
            	bundle.putString("firstname", firstNameText.getText().toString());
            	bundle.putString("lastname", lastNameText.getText().toString());
            	
            	String gender = "";
    			if (genderMaleRadioButton.isChecked())
    				gender = "M";
    			else if (genderFemaleRadioButton.isChecked())
    				gender = "F";
    			
            	bundle.putString("gender", gender);
            	
            	int position = householdLocationSpinner.getSelectedItemPosition();
            	if (position == 0) {
            		bundle.putString("household", householdLocationText.getText().toString());
            	} else if (position == 1){
            		bundle.putString("location", householdLocationText.getText().toString());
            	}  else {
            		bundle.putString("village", householdLocationText.getText().toString());
            	}
            	
            	Intent intent = new Intent();
            	intent.putExtras(bundle);        	
            	setResult(1, intent);
            	finish();
            }
        });
	}
}
