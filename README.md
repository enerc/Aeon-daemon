This app will run an AEON node on your Android phone. 

When the node is synchronized and running, a wallet app can connect locally to check and process payements.
Running a node locally is much safer than using public node.

You will need at least 6GB of internal storage to store the mainnet blockchain.
A 64 bit processor is currently required to run this app.

A packaged app is available on the playstore under the name "AEON Daemon" or at https://play.google.com/store/apps/details?id=org.aeon.aeondaemon.app

If you want to build the app yourself, the first thing is to get AEON on https://github.com/aeonix/aeon, then build binaries for arm64-v8a, and then replace the compiled aeond binary under res/raw.
Then import and build with android studio.
