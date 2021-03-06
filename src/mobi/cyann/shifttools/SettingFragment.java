/**
 * SettingFragment.java
 * Nov 5, 2011 3:12:07 PM
 */
package mobi.cyann.shifttools;

import mobi.cyann.shifttools.PreferenceListFragment.OnPreferenceAttachedListener;
import mobi.cyann.shifttools.services.ObserverService;
import android.content.Intent;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * @author arif
 *
 */
public class SettingFragment extends PreferenceListFragment implements OnPreferenceAttachedListener, OnPreferenceClickListener, OnPreferenceChangeListener {
	public SettingFragment() {
		super(R.layout.setting);
		setOnPreferenceAttachedListener(this);
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen rootPreference, int xmlId) {
		findPreference(getString(R.string.key_shifttools_service)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_load_settings)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_save_settings)).setOnPreferenceClickListener(this);
		findPreference(getString(R.string.key_save_settings)).setOnPreferenceChangeListener(this);
		findPreference(getString(R.string.key_delete_settings)).setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(getString(R.string.key_save_settings))) {
			((EditTextPreference)preference).getEditText().setText("");
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(getString(R.string.key_load_settings))) {
			if(newValue != null && newValue.toString().length() > 0) {
				SettingsManager.loadSettings(getActivity(), newValue.toString());
				MainActivity.restart(getActivity());
			}
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_save_settings))) {
			if(newValue != null && newValue.toString().length() > 0) {
				if(SettingsManager.saveSettings(getActivity(), newValue.toString())) {
					Toast.makeText(getActivity(), getString(R.string.save_success_message), Toast.LENGTH_LONG).show();
				}else {
					Toast.makeText(getActivity(), getString(R.string.save_failed_message), Toast.LENGTH_LONG).show();
				}
			}
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_delete_settings))) {
			if(newValue != null && newValue.toString().length() > 0) {
				SettingsManager.deleteSettings(getActivity(), newValue.toString());
			}
			return true;
		}else if(preference.getKey().equals(getString(R.string.key_shifttools_service))) {
			if(newValue != null) {
				if((Boolean)newValue) {
					ObserverService.startService(getActivity(), true);
				}else {
					ObserverService.stopService(getActivity());
				}
			}
			return true;
		}
		return false;
	}
}
