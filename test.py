import shutil
import os 

dir_path = os.path.dirname(os.path.realpath(__file__))
cwd = os.getcwd()

print("cwd=",cwd)
print("sourceDir=",dir_path)
expanded = os.path.dirname(os.path.realpath("~/Documents/Arduino/libraries"))
expanded = os.path.realpath("Documents/Arduino/libraries")




for k, v in os.environ.items():
    print(k,"=",v)
    
print("expnded","=",expanded)