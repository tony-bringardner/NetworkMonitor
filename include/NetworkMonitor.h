/**
 * 
 */
#ifndef NetworkMonitor_h
#define NetworkMonitor_h
#include <Arduino.h>


#ifdef ESP8266
#include <ESP8266WiFi.h>
#else
#include "WiFi.h"
#endif

#include <WiFiUdp.h>



#define NETWORK_MONITOR_VERSION 5



class NetworkMonitor : public Stream {
  private:
    WiFiUDP adminUdp;
    WiFiUDP udpReciever;
    WiFiUDP udpSender;
    Stream *stream=NULL;    
    Stream *readFrom=NULL;
    String udpServer;
    int udpPort=0;
    int localUdpPort=0;
    long unsigned int timeout=2000;
    long unsigned lastAdvertise=0;
    long unsigned advertisePeriod=1000*60*5; // five minutes
    bool advertise=true; 
    bool inited=false;
    bool debug=false;
    IPAddress adminAddress = IPAddress(224,0,0,252);
    int adminPort = 60000;
    String getAdminResponse();


    
    
  public:
    bool replyToUdpInput=true;  
    bool addPacketNumberToUdp=false;
    bool useSerial=true;
    int maxPrintfLen = 200;
    int sendErrorRetrys=4;
    
    NetworkMonitor();
    void setAdvertisePeriod(long unsigned val) {advertisePeriod=val;}
    long unsigned getAdvertisePeriod() {return advertisePeriod;}
    void setAdvertise(bool val) { advertise=val;}
    bool isAdvertise() { return advertise;}

    void sendAdvertisment();
    void beginUdp(const char *serverName, int serverPort, int udpLocalPort  ) ;
    void beginUdp(const char *serverName, int serverPort);

    int read();
    int available();
    void flush();
    virtual size_t write(uint8_t c) { 
          Serial.println("Invalid call to virtual size_t write(uint8_t c) ");
          return -1;
    };

    bool find(char *target);
    bool find(char *target, size_t len);
    bool findUntil(char *target, char *terminal);
    int peek();
    size_t readBytes(char *buffer, int length);
    size_t readBytesUntil(char character, char *buffer, int length);
    String readString();
    String readStringUntil(char terminator);
    void setDebug(bool value);
    bool isDebug();
    void setTimeout(int val);
    void beginStream(Stream *s);

    void print(char *msg);
    void print(String);
    void println(String);
    void print(float);
    void println(float);
    void print(long);
    void println(long);
    void print(unsigned long);
    void println(unsigned long);
    void print(double);
    void println(double);
    void print(word);
    void println(word);
    
    void print(IPAddress);
    void println(IPAddress);
    
    
    void print(int);
    void println(int);
    void println();
    int  printf(const char* fmt, ...);
    
    
    
   private:
    
    String udpInput;
    int udpIdx=0;
    void readUdp();
    unsigned long udpPacketNumber=0;
    bool waitForAvailible();
    void checkAdmin();
    
};


#endif
