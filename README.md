# Aeon daemon
 
AEON node for your Android phone. This is aeond packaged in an Android App.  


## Installation from github

If you want to build the app yourself, the first thing is to get AEON on https://github.com/aeonix/aeon, then build binaries for arm64-v8a and armeabi, and then replace the compiled aeond binary under res/raw.

res/raw/aeond32 is the arm 32 bit compiled daemon, and res/raw/aeon64 is the 64 bit one.

Then import and build with android studio.  



## Installation from play store

A packaged app is available on the playstore under the name "AEON Daemon" or at https://play.google.com/store/apps/details?id=org.aeon.aeondaemon.app  


## Phone requirements
A 64 bit processor with 6 Gb of storage is recommended to run on the mainnet blockchain.  


## Wallet connection
When the node is synchronized and running, a wallet app can connect locally to check and process payements.
Running a node locally is much safer than using a public node.  


## About Monero
If you want to build this app for Monero, just replace aeon32 and aeon64 by there monero ones, and change ressources and name in MainActivity:copyBinaryFile. That's it!  


### Donations

** XMR **    `4AZSfpPFsLEgxpBVmNdoysYERDQiGu7daKB2WtWgKK1AGqeJBhRp4ZNjVPMARyoSpPb3WkGsQ7p5tKvKex9eJpFqRJQXvQZ`

** AEON **   `Wmsmmjtzk269mpmWm9CTC8DXDs9FZmKdrbFqm1gAmdFxJwEtsZU9PxDJDLYxtLsoSSjn6y6iXYcXVfgYSAGC5vrL13rDqUs4n`

Highly appreciated.  


### License

Licensed under the Apache License, Version 2.0.

http://www.apache.org/licenses/LICENSE-2.0
