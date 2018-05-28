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
import info.hoang8f.widget.FButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static String BINARY_PATH=null;
    private static Launcher launcher=null;
    private static AppCompatPreferenceActivity logActivity;
    private static AppCompatPreferenceActivity aboutActivity;
    private FButton switchButtonSync;
    private boolean isChecked = false;
    private static int ButtonColorStopped=0x39a833;
    private static int ButtonColorStarting=0x9295dd;
    private static int ButtonColorRunning=0xa28a82;
    private static boolean initDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if not initialized - because onCreate is called on screen rotation.
        if (!initDone) copyBinaryFile();

        setContentView(R.layout.activity_main);

        switchButtonSync = (FButton)  findViewById(R.id.synchronize);
        switchButtonSync.setText(R.string.daemon_stopped);
        // Does not work in xml file
        switchButtonSync.setShadowEnabled(true);
        switchButtonSync.setButtonColor(ButtonColorStopped);
        switchButtonSync.setShadowHeight(10);
        switchButtonSync.setCornerRadius(20);

        Gauge gauge = (Gauge)findViewById(R.id.gauge);
        gauge.setValue(getFreeSpace());

        MyAsyncTask myTask = new MyAsyncTask();
        myTask.execute();
        initDone = true;
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

    public void myClickHandler(View target) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        CollectPreferences.collect(preferences);
        if (launcher == null) {
            launcher = new Launcher();
            String storagePath=getApplicationContext().getFilesDir().getPath();
            CollectPreferences.collectedPreferences.setDataDir(storagePath);
        }

        isChecked = ! isChecked;

        if (isChecked) {
            switchButtonSync.setButtonColor(ButtonColorStarting);
            switchButtonSync.setText(R.string.daemon_starting);
            String status = launcher.start(CollectPreferences.collectedPreferences);
            if (status != null) {
                launcher.updateStatus();
                String msg="";
                if (launcher.getLogs().length() > 0) msg = launcher.getLogs();
                else msg = "aeond process failed to start. err="+status;

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
            switchButtonSync.setEnabled(false);
            switchButtonSync.setVisibility(View.INVISIBLE);
            switchButtonSync.setButtonColor(ButtonColorStopped);
            switchButtonSync.setText(R.string.daemon_stopped);
        }
    }

    /**
     * Copy the aeond binary file to a location where wa have execute rights
     */
    private void copyBinaryFile() {
        Resources res = getResources();
        Log.e(TAG," "+is64bitsProcessor());

        InputStream in_s = res.openRawResource(is64bitsProcessor() ? R.raw.aeond64 :R.raw.aeond32);
        try {
            // read aeond binry file from the ressource raw folder
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            String pathName = getApplicationContext().getApplicationInfo().dataDir + "/lib";

            BINARY_PATH=getApplicationContext().getCacheDir().getPath() + "/../aeond";

            // write the file to an android executable location
            OutputStream outputStream = new FileOutputStream(BINARY_PATH);
            outputStream.write(b);
            outputStream.flush();
            outputStream.close();

            // make the file executable
            File f = new File(BINARY_PATH);
            f.setExecutable(true);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(MainActivity.this);
            }
            builder.setTitle("Copy binary file")
                    .setMessage(e.getMessage())
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

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
                    Thread.sleep(4000);
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

                switchButtonSync.setButtonColor(ButtonColorRunning);
                switchButtonSync.setText(R.string.daemon_runnning);

                // re-enable the daemon start button if disabled and process fully stopped
                if (launcher != null && !isChecked && !launcher.isAlive()) {
                    switchButtonSync.setVisibility(View.VISIBLE);
                    switchButtonSync.setEnabled(true);
                    switchButtonSync.setButtonColor(ButtonColorStopped);
                    switchButtonSync.setText(R.string.daemon_stopped);
                }
                // background process is stopped
                if (launcher != null && !launcher.isAlive()) {
                    switchButtonSync.setVisibility(View.VISIBLE);
                    switchButtonSync.setEnabled(true);
                    isChecked = false;
                    switchButtonSync.setButtonColor(ButtonColorStopped);
                    switchButtonSync.setText(R.string.daemon_stopped);
                }
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
                Log.e(TAG,""+logActivity);
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

    public static Launcher getLauncher() {
        return launcher;
    }

    private boolean is64bitsProcessor() {
        String supported[] =  Build.SUPPORTED_ABIS;
        for (String s: supported) {
            if (s.equals("arm64-v8a")) return true;
        }
        return false;
    }
}