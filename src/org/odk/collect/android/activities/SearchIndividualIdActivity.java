package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
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
	private TextView householdText;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.search_individual_layout);
        
        firstNameText = (TextView)findViewById(R.id.firstNameText);
        lastNameText = (TextView)findViewById(R.id.lastNameText);
        genderMaleRadioButton = (RadioButton)findViewById(R.id.male);
        genderFemaleRadioButton = (RadioButton)findViewById(R.id.female);
        searchButton = (Button)findViewById(R.id.filterResults);
        clearButton = (Button)findViewById(R.id.clear);
        householdText = (TextView)findViewById(R.id.householdText);
        
        clearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {  
            	firstNameText.setText("");
            	lastNameText.setText("");
            	genderMaleRadioButton.setChecked(false);
            	genderFemaleRadioButton.setChecked(false);
            	householdText.setText("");
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
            	
            	bundle.putString("household", householdText.getText().toString());
            	
            	Intent intent = new Intent();
            	intent.putExtras(bundle);        	
            	setResult(1, intent);
            	finish();
            }
        });
	}
}
