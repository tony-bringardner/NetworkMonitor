
The Arduino serial monitor can help to debug Arduino software sketches or viewing data sent by a working sketch. 
However, the serial monitor requires your MCU to be connected to your local machine via USB port.
The NetworkMonitor will allow you to monitor and control your network capable MCU remotely.

NetworkMonitor has been tested on ESP8266 and ESP32 but should be compatible with any WIFI enabled MCU.

The NetworkMonitor desktop tool also supports a plotter much like the Arduino Serial Plotter without the USB connection.

If the MCU is connect via USB and the Arduino Serial object is properly configured, serial input and output will work as well.

You can find the NetwrokMonitorDesktop application with the NetworkMonitor library example (/examples/NetworkMonitorExample/NetworkMonitorDesktop.jar) or use the Arduino menu Scetch -> Show sketch folder.

If yo are using the Arduino IDE < 2.0 placing NetworkMonitorDesktop.jar in Arduino path Arduino/tools/NetworkMonitor/tool with make it available from the Arduino 'Tools' menu.



Source code for the desktop application is available at https://github.com/tony-bringardner/NetworkMonitorDesktop

There is also a mobile version of the desktop application yor can run an Android and IOS mobile devices. 
The source code for a mobile version written in Dart/Flutter (https://flutter.dev) is availible at https://github.com/tony-bringardner/NetworkMonitorMobile.
