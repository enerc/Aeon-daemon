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

package org.aeon.aeondaemon.app.model;

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
import android.util.Log;

import org.aeon.aeondaemon.app.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Launcher {
    private static int MAX_LOG_SIZE = 30000;

    private BufferedReader reader=null;
    private BufferedWriter writer=null;
    private String version=null;
    private StringBuffer logs=null;
    private String height;
    private String target;
    private String peers;
    private boolean running=false;
    private Process process=null;

    public boolean start(Settings pref)  {
        try  {
            String env = getEnv(pref);
            Log.e("process" , env);

            // Executes the command.
            process = Runtime.getRuntime().exec(MainActivity.BINARY_PATH+" "+env);

            // maps stdout
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // maps stdin
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        running = true;
        return true;
    }

    /**
     * Collect the logs, get the version string and get current sync status
     *
     * Non blocking I/O
     */
    public void updateStatus() {
        try {
            if (reader != null && reader.ready()) {
                char[] buffer = new char[4096];
                if (logs == null) logs = new StringBuffer();

                while (reader.ready()) {
                    int read = reader.read(buffer);
                    if (read > 0) {
                        logs.append(buffer, 0, read);
                    }
                }

                // truncate if log too big
                if (logs.length() > MAX_LOG_SIZE)
                    logs.delete(0,logs.length() - MAX_LOG_SIZE);

                // Try to get aeond version and build number
                if (version == null) {
                    int i = logs.toString().indexOf("src/daemon/main.cpp");
                    if (i != -1) {
                        int j = logs.toString().substring(i).indexOf("Aeon '");
                        if (j != -1) {
                            int k = logs.toString().substring(i+j).indexOf(")");
                            version =  logs.toString().substring(i+j+5,i+j+k+1);
                        }
                    }
                }

                // Update Height and target
                int i = logs.toString().lastIndexOf("\nHeight");
                if (i > 0) {
                    height="";
                    target="";
                    peers="";
                    while (logs.charAt(i) != ' ') i++;
                    while (logs.charAt(i) != ',') {
                        height += logs.charAt(i);
                        i++;
                    }
                    i+=2;
                    while (logs.charAt(i) != ' ') i++;
                    i++;
                    while (logs.charAt(i) != ' ') {
                        target += logs.charAt(i);
                        i++;
                    }
                    while (logs.charAt(i) != '\n') i++;
                    i++;
                    while (logs.charAt(i) != '\n') i++;
                    i++;
                    while (logs.charAt(i) != ' ') {
                        peers += logs.charAt(i);
                        i++;
                    }
                }

                //System.out.println("---------" + i);
                //System.out.println(logs.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();	// to debugger
        }
    }

    public void getSyncInfo()  {
        try {
            // if process not already terminated
            if (writer != null) {
                writer.write("sync_info\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        try {
            if (writer != null) {
                writer.write("exit\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }

    public String getEnv(Settings pref) {
        ArrayList<String> a = new ArrayList<String>();

        a.add("--data-dir "+pref.getDataDir());
        if (pref.getLogFile() != null) a.add("--log-file "+pref.getLogFile());
        if (pref.getIsTestnet()) a.add("--testnet");
        if (pref.getIsStageNet()) a.add("--stagenet");
        if (pref.getBlockSyncSize() != 0) a.add("--block-sync-size "+pref.getBlockSyncSize());
        if (pref.getZmqRpcPort() != 0) a.add("--zmq-rpc-bind-port "+pref.getZmqRpcPort());
        if (pref.getP2pBindPort() != 0) a.add("--p2p-bind-port "+pref.getP2pBindPort());
        if (pref.getRpcBindPort() != 0) a.add("--rpc-bind-port "+pref.getRpcBindPort());
        if (pref.getAddExclusiveNode() != null) a.add("--add-exclusive-node "+pref.getAddExclusiveNode());
        if (pref.getSeedNode() != null) a.add("--seed-node "+pref.getSeedNode());
        if (pref.getOutPeers() != -1) a.add("--out-peers "+pref.getOutPeers());
        if (pref.getInPeers() != -1) a.add("--in-peers "+pref.getInPeers());
        if (pref.getLimitRateUp() != -1) a.add("--limit-rate-up "+pref.getLimitRateUp());
        if (pref.getLimitRateDown() != -1) a.add("--limit-rate-down "+pref.getLimitRateDown());
        if (pref.getLimitRate() != -1) a.add("--limit-rate "+pref.getLimitRate());
        if (pref.getBoostrapDaemonAdress() != null) a.add("--bootstrap-daemon-address "+pref.getBoostrapDaemonAdress());
        if (pref.getBoostrapDaemonLogin() != null) a.add("--bootstrap-daemon-login "+pref.getBoostrapDaemonLogin());
        if (pref.getPeerNode() != null) a.add("--add-peer "+pref.getPeerNode());
        if (pref.getAddPriorityNode() != null) a.add("--add-priority-node "+pref.getAddPriorityNode());

        String ret = "";
        for (String s : a) {
            ret += s + " ";
        }
        return ret;
    }

    public String getPeers() {
        return peers;
    }

    public String getHeight() {
        return height;
    }

    public String getTarget() {
        return target;
    }


    public String getVersion() {
        return version;
    }

    public String getLogs() {
        return logs == null ? "" : logs.toString();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isAlive() {
        if (process == null) return false;
        try {
            process.waitFor();
        } catch (InterruptedException e){
            return false;
        }
        process = null;
        return true;
    }


}
