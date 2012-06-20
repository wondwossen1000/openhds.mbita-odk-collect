package org.odk.collect.android.logic;

/**
 * A custom record that is shown in the listView rows for displaying OpenHDS entity data
 */
public class CustomItem {

	private String extId;
	private String name;
	
	public String getExtId() {
		return extId;
	}
	
	public void setExtId(String extId) {
		this.extId = extId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
