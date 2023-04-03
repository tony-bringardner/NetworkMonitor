/**
The Arduino serial monitor is to help to debug Arduino software sketches or viewing data sent by a working sketch. However, the serial monitor requires your MCU to be connected to your local machine via USB port.
The Netrwork monitor will alow you to monitor and control your network capable MCU (esp8266,esp32) remotly.

The NetworkMonitor desktop tool also supports a ploiotter much like the Ardionmo Serial Plotter without the USB teather.
*/


#include "NetworkMonitor.h"

String ssid = "bringardner";
String password = "peekab00";

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
int plotDelay=100;

//  Create a monitor
NetworkMonitor monitor;
extern void printTest();




void setup() {
  Serial.begin(115200);
  while (!Serial);
  Serial.println();

  WiFi.begin(ssid.c_str(), password.c_str());

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print("x");
  }
  Serial.println();

  /*
  * Configure the monitor to conenct to any computer on the WAN.
  * Once connected to the network you can take control from any computer on the WAN with the 'acquire' function.
  */
  monitor.beginUdp("192.168.1.212",6000,6000);
  
  monitor.setTimeout(10000);
  /*  
   * By defult NetworkMonitor will include Serial in all operations.  
   * You can excplude Serial by setting useSerial = false;
   */
  monitor.useSerial=true;
  /*
   * NeworkMonitor uses UDP and as a result some packets may not reach it's destination.
   * Set addPacketNumberToUdp = true and the monitor will add a packet number to each packet
   */
  monitor.addPacketNumberToUdp = false;

  /*
  * Turn on advertising to broadcast your existendce to all network monitors on the WAN
  */
  monitor.setAdvertise(true);
  /*
  *  Frequency of Braodcasts
  */
  monitor.setAdvertisePeriod(60000);
  
  monitor.println("Network monitor configured\nip="+WiFi.localIP().toString());
  
  printTest();
  
}

void printTest() {
  //  print one of each data types
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
  monitor.print((char*)"Exit Print   test");
}

void plotSinWave() {
  for(int i = 0; i < 360; i += 5) {
    float y1 = 1 * sin(i * M_PI / 180);
    float y2 = 2 * sin((i + 90)* M_PI / 180);
    float y3 = 5 * sin((i + 180)* M_PI / 180);

    monitor.print(y1);
    monitor.print("\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.print(y2);
    monitor.print("\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.println(y3); // the last value is followed by a carriage return and a newline characters.

    delay(plotDelay);
  }
}

int plotIdx = 0;

void plotSinWave2() {
    float y1 = 1 * sin(plotIdx * M_PI / 180);
    float y2 = 2 * sin((plotIdx + 90)* M_PI / 180);
    float y3 = 5 * sin((plotIdx + 180)* M_PI / 180);

    monitor.print(y1);
    monitor.print("\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.print(y2);
    monitor.print("\t"); // a space ' ' or  tab '\t' character is printed between the two values.
    monitor.println(y3); // the last value is followed by a carriage return and a newline characters.

    if( (plotIdx += 5) >= 360) {
      plotIdx=0;
    }
    
}

int parseValue(String val) {
  int ret = 0;
  int idx=val.indexOf('=');
  if( idx > 0 ) {
    ret = val.substring(idx+1).toInt();
  }
      
  return ret;
}

void loop() {

  unsigned long cur = millis();
  if ( cur - lastPrintTest > 900000) {    
    printTest();
    lastPrintTest = cur;    
  }

  //  read any input that's aiavailable and process it
  int cnt = monitor.available();

  if (cnt>0) {
    Serial.println(" read string expect ="+String(cnt));
    unsigned long start = millis();
    String line = monitor.readStringUntil('\n');
    line.trim();
    monitor.println("Line from monitor='" + line + "' cnt="+String(cnt)+" len="+String(line.length())+" time to read=" + String(millis() - start));

    if( line == "test") {    
      printTest();    
    } else if( line.startsWith("plot")) {
      int idx=line.indexOf(' ');
      while( idx > 0 ) {
        line = line.substring(idx+1);
        monitor.println("new line='"+line+"'");
        if(line.startsWith("on")) {
          plot = true;
          plotIdx=0;
        } else if(line.startsWith("off")) {
          plot = false;
        } else if(isDigit(line[0])) {
          plotDelay = line.toInt();
        }        
        idx=line.indexOf(' ');
      }
      monitor.println("Plot "+(plot?String("on"):String("off"))+" delay="+String(plotDelay));              
    }    
  }

  if( plot  && (millis()-lastPlot)>= plotDelay) {
      plotSinWave2();      
      lastPlot = millis();
    }

}
