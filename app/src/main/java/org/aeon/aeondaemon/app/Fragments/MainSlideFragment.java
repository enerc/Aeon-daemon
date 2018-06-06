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
package org.aeon.aeondaemon.app.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.aeon.aeondaemon.app.MainActivity;
import org.aeon.aeondaemon.app.R;
import org.aeon.aeondaemon.app.model.CollectPreferences;
import org.aeon.aeondaemon.app.model.Launcher;

import java.io.File;

public class MainSlideFragment extends Fragment {
    private static final String TAG = MainSlideFragment.class.getSimpleName();
    private static long RefreshInterval = 5000;
    private static Launcher launcher = null;
    private ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.main_fragment, container, false);

        MyAsyncTask myTask = new MyAsyncTask();
        myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            while (true) {
                boolean hasFocus =  MainActivity.getmViewPager().getCurrentItem() == MainActivity.FRAGMENT_MAIN;
                if (hasFocus) {
                    if (launcher == null) {
                        launcher = new Launcher();
                        updatePreferences();
                    }

                    // The process died or was not started
                    if (launcher.isStopped()) {
                        // Restart the background process
                        updatePreferences();        // properties may have been changed in the settings.
                        String status = launcher.start(CollectPreferences.collectedPreferences);
                        if (status != null) {
                            launcher.updateStatus();
                            String msg = "";
                            if (launcher.getLogs().length() > 0) msg = launcher.getLogs();
                            else msg = "aeond process failed to start. err=" + status;

                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(getActivity());
                            }
                            builder.setTitle("aeond")
                                    .setMessage(msg)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                    if (launcher.isAlive()) {
                        launcher.updateStatus();
                        launcher.getSyncInfo();
                        publishProgress("update");
                    }
                }
                try {
                    Thread.sleep(RefreshInterval);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        protected void onProgressUpdate(String... text) {
            Log.d(TAG,"onProgressUpdate");
            try {
                if (launcher != null && launcher.isAlive()) {
                    TextView v = (TextView) rootView.findViewById(R.id.heightValue);
                    v.setText(launcher.getHeight());

                    v = (TextView) rootView.findViewById(R.id.heightTarget);
                    v.setText(launcher.getTarget());

                    v = (TextView) rootView.findViewById(R.id.compiledMsgAeonVersion);
                    if (launcher.getVersion() != null) v.setText(launcher.getVersion());

                    v = (TextView) rootView.findViewById(R.id.peers);
                    if (launcher.getPeers() != null)
                        v.setText(launcher.getPeers() + " " + getActivity().getString(R.string.msg_peers_connected));

                    v = (TextView) rootView.findViewById(R.id.downloading);
                    if (launcher.getDownloading() != null)
                        v.setText(getActivity().getString(R.string.download_at) + " " + launcher.getDownloading() + " kB/s");

                    v = (TextView) rootView.findViewById(R.id.disk);
                    String s = String.format("%.1f", getUsedSpace());
                    v.setText(s + " " + getActivity().getString(R.string.disk_used));
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        protected void onPostExecute(String result) {
        }
    }

    /**
     * Get availabe free space on the disk
     *
     * @return percentage of free space.
     */
    private float getUsedSpace() {
        File f = new File(CollectPreferences.collectedPreferences.isUseSDCard() ? CollectPreferences.collectedPreferences.getSdCardPath() : MainActivity.BINARY_PATH);
        return f.getFreeSpace() / 1024.0f / 1024.0f / 1024.0f;
    }

    public static Launcher getLauncher() {
        return launcher;
    }

    private void updatePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        CollectPreferences.collect(preferences);
        // If using internal storage, set the path for it.
        if (!CollectPreferences.collectedPreferences.isUseSDCard()) {
            String storagePath = getActivity().getApplicationContext().getFilesDir().getPath();
            CollectPreferences.collectedPreferences.setDataDir(storagePath);
        } else {
            CollectPreferences.collectedPreferences.setDataDir(CollectPreferences.collectedPreferences.getSdCardPath());
        }
    }
}
