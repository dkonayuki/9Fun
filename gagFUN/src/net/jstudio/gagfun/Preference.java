package net.jstudio.gagfun;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference);
        //CheckBoxPreference safeMode = (CheckBoxPreference) findPreference("safeMode");
        //safeMode.setEnabled(false);
    }
 
}
