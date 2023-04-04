#include "NetworkMonitor.h"

NetworkMonitor::NetworkMonitor() {   

}


int NetworkMonitor::printf(const char* fmt, ...){
  if (!fmt || maxPrintfLen<=0) {
        return -1;
   }
    
  va_list args;
  va_start(args,fmt);
  char out[maxPrintfLen+1];
  int len = vsnprintf(out, maxPrintfLen, fmt, args)-1;
  va_end(args);
  print(String(out));
  return len>maxPrintfLen?maxPrintfLen:len;
}

void NetworkMonitor::setDebug(boolean val) {
  debug = val;
}

boolean NetworkMonitor::isDebug() {
  return debug;
}
void NetworkMonitor::setTimeout(int val) {

  timeout = val;
  Serial.setTimeout(val);
  udpReciever.setTimeout(val);
  
  if (stream ) {
    stream->setTimeout(val);
  }
}




void NetworkMonitor::beginUdp(const char *serverName, int serverPort, int udpLocalPort  ) {
  //Serial.println("Enter begindUdp ");
  NetworkMonitor::udpServer = String(serverName);
  NetworkMonitor::udpPort = serverPort;
  NetworkMonitor::localUdpPort = udpLocalPort;
  //Serial.println(" begindUdp "+NetworkMonitor::udpServer+" sp="+String(serverPort)+" lp="+String(udpLocalPort));
  udpReciever.begin(udpLocalPort);
  
  
  adminUdp.begin(adminPort);
  
  #ifdef ESP8266
  adminUdp.beginMulticast(WiFi.localIP(), adminAddress, adminPort);
  #else 
  adminUdp.beginMulticast( adminAddress, adminPort);
  #endif
  

  //Serial.println("Exit begindUdp");

}

void NetworkMonitor::beginUdp(const char *serverName, int serverPort) {
    beginUdp(serverName,serverPort,serverPort);      
}

bool NetworkMonitor::find(char *target) {
  bool ret = false;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret = readFrom->find(target);
  }

  return ret;
}

bool NetworkMonitor::find(char *target, size_t len) {
  bool ret = false;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->find(target, len);
  }

  return ret;
}

bool NetworkMonitor::findUntil(char *target, char *terminal) {
  bool ret = false;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->findUntil(target, terminal);
  }
  return ret;
}

void NetworkMonitor::flush() {
  Serial.flush();
  udpReciever.flush();
  if ( stream ) stream->flush();
}

int NetworkMonitor::peek() {
  int ret = -1;
  available();

  if ( readFrom ) {
    ret =  readFrom->peek();
  }
  
  return ret;
}


bool NetworkMonitor::waitForAvailible() {
  int ret = available();
  if ( ret == 0) {
    long unsigned start = millis();
    while (((start - millis()) < timeout) && ret == 0 ) {
      delay(1);
      ret = available();
    }
  }

  return ret > 0;
}

size_t NetworkMonitor::readBytes(char *buffer, int length) {
  size_t ret = -1;

  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->readBytes(buffer, length);
  }

  return ret;
}

size_t NetworkMonitor::readBytesUntil(char character, char *buffer, int length) {
  size_t ret = -1;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->readBytesUntil(character, buffer, length);
  }

  return ret;
}


String NetworkMonitor::readString() {
  String ret;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->readString();
  }

  return ret;
}

String NetworkMonitor::readStringUntil(char terminator) {
  String ret;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->readStringUntil(terminator);
  }
  return ret;
}

void NetworkMonitor::beginStream(Stream *s) {
  stream = s;
}


void NetworkMonitor::checkAdmin() {
  
  int cnt = 0;
  
  if ( (cnt=adminUdp.parsePacket()) > 0 ) { 
  
    String cmd = "";
    String target = "";
    String host="";
    int port=-1;
    String tmp="";
    while( cnt-- ) {
        tmp += String((char)adminUdp.read());
    }
    tmp.toLowerCase();
    
    int idx= tmp.indexOf(' ');
    if( idx > 0 ) {
      cmd = tmp.substring(0,idx);
      tmp = tmp.substring(idx+1);
      idx= tmp.indexOf(' ');
      if( idx > 0 ) {
        host = tmp.substring(0,idx);
        port = tmp.substring(idx+1).toInt();
        int idx2= tmp.indexOf(' ',idx+1);
        if( idx2 > 0 ) {
          target = tmp.substring(idx2+1);
        }
      }
    }

    Serial.println("cmd="+cmd+" host = "+host+" port="+String(port)+" target="+target);

    if( cmd == "acquire" ){
      if( target == WiFi.localIP().toString() ){
          if( udpServer != host || port != udpPort) {
            println("Admin control acquired by "+host+":"+String(port));      
            delay(10);
            beginUdp(host.c_str(),port);
            delay(10);
          }
      }
    } 

    //  reply with current setup
      String msg = getAdminResponse();              
      udpSender.beginPacket(host.c_str(), udpPort);
      udpSender.print(msg);
      udpSender.endPacket();
      Serial.println("sent="+msg+" host = "+host+" port="+String(udpPort));
      
  } else {
    if( advertise ) {
      if( (millis()-lastAdvertise) > advertisePeriod) {
       #ifdef ESP8266
       adminUdp.beginPacketMulticast(adminAddress,adminPort,WiFi.localIP(),255);
       #else
       adminUdp.beginMulticastPacket();
       #endif
       adminUdp.print(getAdminResponse());
       adminUdp.endPacket();
       lastAdvertise = millis();
      }
    }
  }
  
} 


String NetworkMonitor::getAdminResponse() {
  
  return String(ARDUINO_BOARD)+String( " Monitored by ")+udpServer+" "+String(udpPort)+"\n";              
}

int NetworkMonitor::available()    {
  checkAdmin();
  int ret = 0;
  readFrom = NULL;
  if (useSerial && (ret = Serial.available()) > 0 ) {
    readFrom = &Serial;
  } else if (stream && (ret = stream->available()) > 0) {
    readFrom = stream;
  } else if ( (ret = udpReciever.available()) > 0) {
    readFrom = &udpReciever;
  } else if ( (ret = udpReciever.parsePacket()) > 0 ) {
    readFrom = &udpReciever;
  } 

  return ret;
}


void NetworkMonitor::println() {
  NetworkMonitor::print((char*)"\r\n");
}

void NetworkMonitor::println(String msg) {
  NetworkMonitor::print(msg + "\r\n");
}

void NetworkMonitor::println(int msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(int msg) {
  NetworkMonitor::print(String(msg));
}

void NetworkMonitor::println(word msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(word msg) {
  NetworkMonitor::print(String(msg));
}



void NetworkMonitor::println(float msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(float msg) {
  NetworkMonitor::print(String(msg));
}
void NetworkMonitor::println(double msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(char *msg) {
  NetworkMonitor::print(String(msg));
}

void NetworkMonitor::print(IPAddress msg) {
  NetworkMonitor::print(msg.toString());
}
void NetworkMonitor::println(IPAddress msg) {
  NetworkMonitor::print(msg.toString() + "\r\n");
}

void NetworkMonitor::println(long msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(long msg) {
  NetworkMonitor::print(String(msg));
}

void NetworkMonitor::println(unsigned long msg) {
  NetworkMonitor::print(String(msg) + "\r\n");
}

void NetworkMonitor::print(unsigned long msg) {
  NetworkMonitor::print(String(msg));
}


void NetworkMonitor::sendAdvertisment() {

}

void NetworkMonitor::print(String msg) {

  if( useSerial ) {
    Serial.print(msg);
  }
  if ( stream ) {
    stream->print(msg);
  }

  if ( udpPort > 0 ) {
    if( WiFi.isConnected()  ) {
      if ( addPacketNumberToUdp ) {
        msg = "("+String(++udpPacketNumber) + ") " + msg;
      }
    
    
      //Serial.println("svr="+udpServer+" udpPort ="+String(udpPort));
      int tries = 0;
      int res = 0;

      while(!res && tries < sendErrorRetrys) {
      if((res=udpSender.beginPacket(udpServer.c_str(), udpPort))) {
        udpSender.print(msg);
        
        if(!(res=udpSender.endPacket())){
          tries++;
          //Serial.println("send failed tries "+String(tries));
          yield();
        } 
      } else {
          tries++;
          //Serial.println("begin failed tries="+String(tries));
          yield();
      }
    }

    if( !res ){
        Serial.println("udp send failed tries="+String(tries)+" res="+String(res));
    }

    //yield();
  } else {
    //Serial.println("WiFi is not connewcted ");
  }
  } else {
    //Serial.println("udpPort is <=0 ="+String(udpPort));
  }
}


/*
   Returns:The first byte of incoming data available (or -1 if no data is available).
*/
int NetworkMonitor::read() {
  int ret = -1;
  if ( waitForAvailible() ) {
    if ( readFrom ) ret =  readFrom->read();
  }
  return ret;
}
