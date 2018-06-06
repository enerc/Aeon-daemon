/**
 * Copyright (c) 2018 enerc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aeon.aeondaemon.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import org.aeon.aeondaemon.app.Fragments.MainSlideFragment;
import org.aeon.aeondaemon.app.model.CollectPreferences;
import org.aeon.aeondaemon.app.model.Launcher;

public class SettingsPrefActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsPrefActivity.class.getSimpleName();
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key)  {
                        Launcher launcher = MainSlideFragment.getLauncher();
                        if (launcher != null) {
                            Log.e(TAG, "Stop aeon daemon");
                            launcher.exit();
                        }

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        // If SD card enabled set a preference if empty
                        String path = preferences.getString("sd_storage","");
                        boolean used = preferences.getBoolean("use_sd_card",false);
                        // Card has been inserted and "Use SD card" checked
                        if (used && path.equals("")) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("sd_storage",CollectPreferences.getExternalStoragePath());
                            editor.commit();

                            getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

                        }
                        Log.e(TAG,path+" " + used);

                    }
                };
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(listener);

        String sdLocation = CollectPreferences.getExternalStoragePath();
        // if the SD card has been removed.
        if (sdLocation == null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("use_sd_card",false);
            editor.commit();
        }

        Log.d(TAG,"sdLocation:"+ sdLocation);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}