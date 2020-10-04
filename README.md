# Aeon daemon
 
AEON node for your Android phone. This is aeond packaged in an Android App.  


## Installation from github binaries

Make sure you enabled unknown sources in Android configuration (settings -> Security).

Then download the app in the release section. 




## Build it yourself

If you want to build the app yourself, the first thing is to get AEON on https://github.com/aeonix/aeon, then [build binaries for arm64-v8a and armeabi](https://github.com/BigslimVdub/AeonAndroidArmV8), and then replace the compiled aeond binary under res/raw.

res/raw/aeond32 is the arm 32 bit compiled daemon, and res/raw/aeon64 is the 64 bit one.

Then import and build with android studio.  


## Phone requirements
A 64 bit processor with 6 Gb of storage is recommended to run on the mainnet blockchain.  


## Wallet connection
When the node is synchronized and running, a wallet app can connect locally to check and process payements.
Running a node locally is much safer than using a public node.  


## About Monero
If you want to build this app for Monero, just replace aeon32 and aeon64 by there monero ones, and change ressources and name in MainActivity:copyBinaryFile. That's it!  

## Tips
If your phone gets pretty hot during synchronization, go to "settings" -> "Limit rate" and set a value around 50 kB/s. This will reduce CPU usage but will make synchronization slower....  


### Donations

** XMR **    `4AZSfpPFsLEgxpBVmNdoysYERDQiGu7daKB2WtWgKK1AGqeJBhRp4ZNjVPMARyoSpPb3WkGsQ7p5tKvKex9eJpFqRJQXvQZ`

** AEON **   `Wmsmmjtzk269mpmWm9CTC8DXDs9FZmKdrbFqm1gAmdFxJwEtsZU9PxDJDLYxtLsoSSjn6y6iXYcXVfgYSAGC5vrL13rDqUs4n`

Highly appreciated.  


### License

Licensed under the Apache License, Version 2.0.

http://www.apache.org/licenses/LICENSE-2.0
