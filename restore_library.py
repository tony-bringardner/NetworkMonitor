import shutil


inFiles = [
    "/Users/tony/Documents/Arduino/libraries/NetworkMonitor/src/NetworkMonitor.h",
    "/Users/tony/Documents/Arduino/libraries/NetworkMonitor/src/NetworkMonitor.cpp",
    "/Users/tony/Documents/Arduino/libraries/NetworkMonitor/examples/NetworkMonitorV5/NetworkMonitorV5.ino"
    
];

outFiles = [
    "/Users/tony/Documents/PlatformIO/Projects/NetworkMonitor/include/NetworkMonitor.h",
    "/Users/tony/Documents/PlatformIO/Projects/NetworkMonitor/src/NetworkMonitor.cpp",
    "/Users/tony/Documents/PlatformIO/Projects/NetworkMonitor/src/NetworkMonitorV5.cpp",
];


for x in range(len(inFiles)):
    print(inFiles[x],"->",outFiles[x])    
    if( x == 2) :
        
        with open(inFiles[x],"r") as inf:
            content = inf.read().replace("bringardner","SSID").replace("peekab00","SSID_PASSWORD");
            with open(outFiles[x],"w") as outf:
                outf.write(content)
    else :        
        shutil.copy(inFiles[x],outFiles[x])
        

