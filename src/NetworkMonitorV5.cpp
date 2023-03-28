/**

*/


#include "NetworkMonitor.h"

String ssid = "bringardner";//"ARRIS-F86C";
String password = "peekab00";

//  One of each native data types for testing
boolean debug = false;
unsigned long lastPrintTest = 0;
int i = 23;
float f = 23.23;
long l = 1234;
unsigned long ul = 321;
boolean b = true;
char  c = 'c';
byte  bb = 12;
word w = 23;
short s = 43;
double d = 34.54;


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
  monitor.setDebug(false);
  monitor.setAdvertise(true);
  
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
  monitor.print((char*)"boolean ");
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
    }
  }

}
