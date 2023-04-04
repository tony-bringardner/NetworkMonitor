
NetworkMonitor for Arduino project is an Arduino tool and library that provides inout and output similar to the Arduino SerialMonitor. It provides for remote logging and debugging any WiFI enabled MCU (only tested with esp8266 & esp32) and is ideal for managing devices in the field when the SerialMonitor is not an option.


The library creates a Stream object, which can is code compatible with the 'Serial' object.  Input and output are availible through the NetworkMonitor desktop tool.
It the MCU is connected via UB adn the Serial object is properly initialized, input and output to and from 'Serial' object as well.

