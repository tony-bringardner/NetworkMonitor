
NetworkMonitor for Arduino project is an Arduino tool and library that provides inout and output similar to the Arduino SerialMonitor. It provides for remote logging and debugging any WiFI enabled MCU (only tested with esp8266 & esp32) and is ideal for managing devices in the field when the SerialMonitor is not an option.


The library creates a Stream object, which can be used the same way as the 'Serial' object but the input and output output is sent to 'Serial' and a UDP socket.


The library is in Library Manager. You can install it there.