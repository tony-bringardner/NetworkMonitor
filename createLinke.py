import io
from os.path import exists
import os


ino = "/Users/tony/Documents/Arduino/libraries/NetworkMonitor/examples/NetworkMonitorExample/NetworkMonitorExample.ino"
cpp = "/Users/tony/Documents/Arduino/libraries/NetworkMonitor/src/NetworkMonitorExample.cpp"
if exists(cpp):
    print(cpp,"alread exisit")
else:
    print("linking ",cpp)
    os.link(ino,cpp)
