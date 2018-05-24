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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;

import org.aeon.aeondaemon.app.model.CollectPreferences;
import org.aeon.aeondaemon.app.model.Launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.nitri.gauge.Gauge;

public class MainActivity extends AppCompatActivity {
    public static final String BINARY_PATH="/data/data/org.aeon.aeondaemon.app/aeond";
    private Launcher launcher=null;
    private static AppCompatPreferenceActivity logActivity;
    private static AppCompatPreferenceActivity aboutActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        copyBinaryFile();
        setContentView(R.layout.activity_main);

        Switch switchButtonSync = (Switch)  findViewById(R.id.synchronize);
        switchButtonSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                CollectPreferences.collect(preferences);
                if (launcher == null) {
                    launcher = new Launcher();
                    String storagePath=getApplicationContext().getFilesDir().getPath();
                    CollectPreferences.collectedPreferences.setDataDir(storagePath);
                }

                if (isChecked) {
                    boolean status = launcher.start(CollectPreferences.collectedPreferences);
                    if (!status) {
                        launcher.updateStatus();
                        String msg="";
                        if (launcher.getLogs().length() > 0) msg = launcher.getLogs();
                        else msg = "aeond process failed to start";

                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(MainActivity.this);
                        }
                        builder.setTitle("aeond")
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
                else {
                    launcher.exit();
                    buttonView.setEnabled(false);
                    buttonView.setVisibility(View.INVISIBLE);
                }
            }
        });
        Gauge gauge = (Gauge)findViewById(R.id.gauge);
        gauge.setValue(getFreeSpace());

        MyAsyncTask myTask = new MyAsyncTask();
        myTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
            return true;
        }
        if (id == R.id.action_log) {
            // launch log activity
            startActivity(new Intent(MainActivity.this, LogActivity.class));
            return true;
        }
        if (id == R.id.action_about) {
            // launch about activity
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Copy the aeond binary file to a location where wa have execute rights
     */
    private void copyBinaryFile() {
        Resources res = getResources();
        InputStream in_s = res.openRawResource(R.raw.aeond);
        try {
            // read aeond binry file from the ressource raw folder
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            String pathName = getApplicationContext().getApplicationInfo().dataDir + "/lib";

            // write the file to an android executable location
            OutputStream outputStream = new FileOutputStream(BINARY_PATH);
            outputStream.write(b);
            outputStream.close();

            File f = new File(BINARY_PATH);
            f.setExecutable(true);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
    }

    /**
     * Get availabe free space on the disk
     *
     * @return percentage of free space.
     */
    private float getFreeSpace() {
        File f = new File(BINARY_PATH);
        return 100.0f-(100.0f*f.getFreeSpace())/f.getTotalSpace();
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... params) {
            while(true) {
                if (launcher != null) {
                    launcher.updateStatus();
                    launcher.getSyncInfo();
                    publishProgress("update");
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e){
                     e.printStackTrace();
                }
            }
        }

        protected void onProgressUpdate(String... text)  {
            try {
                if (launcher != null && launcher.isRunning()) {
                    TextView v = (TextView) findViewById(R.id.heightValue);
                    v.setText(launcher.getHeight());

                    v = (TextView) findViewById(R.id.heightTarget);
                    v.setText(launcher.getTarget());

                    v = (TextView) findViewById(R.id.compiledMsgAeonVersion);
                    if (launcher.getVersion() != null) v.setText(launcher.getVersion());

                    v = (TextView) findViewById(R.id.peers);
                    if (launcher.getPeers() != null)
                        v.setText(launcher.getPeers() + " peers connected");

                    if (logActivity != null) {
                        v = (TextView) logActivity.findViewById(R.id.logs);
                        if (launcher.getLogs() != null) v.setText(launcher.getLogs());
                    }

                    Gauge gauge = (Gauge) findViewById(R.id.gauge);
                    gauge.setValue(getFreeSpace());

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String currentDateandTime = sdf.format(new Date());
                    v = (TextView) findViewById(R.id.lastUpdate);
                    v.setText(currentDateandTime);
                }
                // re-enable the daemon start button if disables and process fully stopped
                CompoundButton buttonView = (CompoundButton) findViewById(R.id.synchronize);
                if (launcher != null && !buttonView.isEnabled() && !launcher.isAlive()) {
                    buttonView.setVisibility(View.VISIBLE);
                    buttonView.setEnabled(true);
                }
            } catch (Exception e) {
                Log.e("error",e.getMessage());
                Log.e("error",""+logActivity);
                e.printStackTrace();    // might go wrong here due to app being hibernated
            }
        }

        protected void onPostExecute(String result) {
        }
    }

    public static void setLogActivity(AppCompatPreferenceActivity _logActivity) {
        logActivity = _logActivity;
    }

    public static void setAboutActivity(AppCompatPreferenceActivity _aboutActivity) {
        aboutActivity = _aboutActivity;
    }

}