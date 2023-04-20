/**
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
*/


#include "NetworkMonitor.h"

//  Set these values to match your environment
#define  SSID "bringardner"
#define SSID_PASSWORD  "peekab00"
#define DESKTOP_ADDRESS "192.168.1.212"

//  Create a monitor
NetworkMonitor monitor;

//  One of each native data types for testing
bool debug = false;
unsigned long lastPrintTest = 0;
int i = 23;
float f = 23.23;
long l = 1234;
unsigned long ul = 321;
bool b = true;
char  c = 'c';
byte  bb = 12;
word w = 23;
short s = 43;
double d = 34.54;
bool plot=false;
unsigned long lastPlot = 0;
int plotInterval=100;
int plotIdx = 0;
extern void printTest();




void setup() {
  //  Set up the Ardionl Serial object as normal
  Serial.begin(115200);
  while (!Serial);
  Serial.println();

  //  Connect to wifi
  WiFi.begin(SSID, SSID_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print("x");
  }
  Serial.println();

  /*
  * Now configure the monitor to communicate with with your primary workstation where you will run the Desktop application.
  * Once connected to the network you can run the desktop application on any machine on your WAN and take control using the desktop application 'acquire' function.
  */
  monitor.beginUdp( DESKTOP_ADDRESS );
  
  //  Now you can use the monitor intead of teh Serial object but , if the MCU is connected via USB you can still use the Arduino Serial monitor.
  monitor.println("Network monitor configured\nip="+WiFi.localIP().toString());
  
  printTest();
  
}

void printTest() {
  //  Very simple test / example  print one of each data types
  //  The monitor is code/syntax compatible with the Serial object.

  monitor.print((char*)"Enter Print   test");
  monitor.print((char*)"Testing from ");
  monitor.println(__FILE__);
  monitor.print((char*)"WiFi connected ip = ");
  monitor.println(WiFi.localIP().toString());
  monitor.print((char*)"int ");
  monitor.println(i);
  monitor.print((char*)"float ");
  monitor.println(f);
  monitor.print((char*)"long ");
  monitor.println(l);
  monitor.print((char*)"unsigned long ");
  monitor.println(ul);
  monitor.print((char*)"bool ");
  monitor.println(b);
  monitor.print((char*)"char ");
  monitor.println(c);
  monitor.print((char*)"byte ");
  monitor.println(bb);
  monitor.print((char*)"word ");
  monitor.println(w );
  
  monitor.print((char*)"short ");
  monitor.println(s);
  monitor.print((char*)"double ");
  monitor.println(d);
  monitor.print((char*)"char ");
  monitor.println('N');
  int len = monitor.printf("printf %02d/%02d/%04d %02d:%02d:%04d\n", 10, 28, 2022, 6, 30, 12);
  monitor.println("printf len1="+String(len));
  len = monitor.printf("printf %02d/%02d/%04d %02d:%02d:%04d\t%s\n", 10, 28, 2022, 6, 30, 12, "This is a time format");
  monitor.println("printf len2="+String(len));
  len = monitor.printf("\tprintf %s =%02d\n", "int",10);
  monitor.println("printf len3="+String(len));
  len = monitor.printf("\tprintf %s =%0.2f\n", "float",123.4567);
  monitor.println("printf len4="+String(len));
  monitor.maxPrintfLen = 10;
  len = monitor.printf("printf %02d/%02d/%04d %02d:%02d:%04d\t%s\n", 10, 28, 2022, 6, 30, 12, "This is a time format");
  monitor.maxPrintfLen = 200;
  monitor.println("\nprintf should be 10 len5="+String(len));
  int peek = monitor.peek();
  monitor.println("\npeek ="+String(peek));
  monitor.println((char*)"Exit Print   test");
}



/**
 * Example of how to write data for the Plotter. 
 */
void plotSinWave() {
    float y1 = 1 * sin(plotIdx * M_PI / 180);
    float y2 = 2 * sin((plotIdx + 90)* M_PI / 180);
    float y3 = 5 * sin((plotIdx + 180)* M_PI / 180);

    monitor.print(y1);
    monitor.print((char*)"\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.print(y2);
    monitor.print((char*)"\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.println(y3); // the last value is followed by a carriage return and a newline characters.

    if( (plotIdx += 5) >= 360) {
      plotIdx=0;
    }
    
}


void loop() {

  unsigned long cur = millis();
  if ( cur - lastPrintTest > 900000) {    
    printTest();
    lastPrintTest = cur;    
  }

  //  read any input that's aiavailable and process it.
  //  input might be from Arduino Serial monitor or the NetworkMonitor desktop or mobile application.
  int cnt = monitor.available();

  if (cnt>0) {
    //  If input is avaiblible read it as a string
    unsigned long start = millis();
    String line = monitor.readStringUntil('\n');
    line.trim();
    monitor.println("Line from monitor='" + line + "' cnt="+String(cnt)+" len="+String(line.length())+" time to read=" + String(millis() - start));

    //  redo the print test
    if( line == "test") {    
      printTest();    
    } else if( line.startsWith("plot")) {
      /**
       * This code set's us up to generate data to test the Desktop plotter (Not availible in the Mobile application) 
       * 
       * Format:  plot [on/off] interval
       * Example: plot on 1000    (turn on plot data at one second intervals)
       */
      int idx=line.indexOf(' ');
      while( idx > 0 ) {
        line = line.substring(idx+1);
        if(line.startsWith("on")) {
          plot = true;
          plotIdx=0;
        } else if(line.startsWith("off")) {
          plot = false;
        } else if(isDigit(line[0])) {
          plotInterval = line.toInt();
        }        
        idx=line.indexOf(' ');
      }
      monitor.println("Plot "+(plot?String("on"):String("off"))+" delay="+String(plotInterval));              
    }    
  }

  if( plot  && (millis()-lastPlot)>= plotInterval) {
      plotSinWave();      
      lastPlot = millis();
    }

}
