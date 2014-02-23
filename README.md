Notus
=====

Android logging app for Zephyr HxM BT


Prerequisites
=============

The android bundle: 
See http://developer.android.com/sdk/installing/bundle.html
The 'android' and 'adb' commands below assume that you have added
</path/to/adt-bundle>/sdk/tools and </path/to/adt-bundle>/sdk/platform-tools
to your PATH.

The HxMBT.jar from the 'HxM™ BT Developer Kit' provided by Zephyr (TM).
See http://www.zephyranywhere.com/zephyr-labs/development-tools/.
Currently this links to https://app.box.com/shared/c169gssedk2nrgu4t41f.
Unzip and extract HxM SDK 9700.0124.v1d/HxM Example Android Project/HxMBT.jar.

Building
========

Copy the HxMBT.jar extracted above to Notus/libs/HxMBT.jar.

Run the command :-
android update project --path </path/to/Notus>
to generate local.properties.

Build with :-
ant debug

Installing
==========

adb -d install </path/to/Notus>/bin/Notus-debug.apk


Running
=======

Start the Notus app.
Click the Connect button.
Assuming a successful connection (Status Message is 'Connected' and
stats start updating), click `Start Log'. This will write to a log file
under 'notus' on your Android device. You can then click `Stop Log' to
end data logging.
