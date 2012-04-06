package net.jstudio.gagfun;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Preference extends PreferenceActivity{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference);
        //CheckBoxPreference safeMode = (CheckBoxPreference) findPreference("safeMode");
        //safeMode.setEnabled(false);
        final CheckBoxPreference chkSafeMode = (CheckBoxPreference)findPreference(PublicResource.sSafeMode);
        chkSafeMode.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(
					android.preference.Preference preference) {
				if(PublicResource.getLogged(getBaseContext())){
					return true;
				}else{
					chkSafeMode.setChecked(true);
					Toast.makeText(getBaseContext(), R.string.OnlySafeModeOn, Toast.LENGTH_SHORT).show();
				}
				return false;
			}
        });
    }
	
}
