# Alpdesk-Automationservice
Java-Service for Automation (like Homeautomation)

1)
Install Rasspbian PI OS Lite on RaspberryPi

2)
To enable ssh at first Boot create an empty file named "ssh" directly on the SD-Card using your PC

3)
Boot RaspberryPi from SD-Card and login using "ssh pi@PIIP"
Default-PW is "raspberry"

4)
change pi and root PW by "sudo passwd root" and "sudo passwd pi"

5)
Make updates by "sudo apt update" and "sudo apt upgrade" and reboot after update process by "sudo reboot"

6)
Relogin using SSH and run "sudo raspi-config"
DO following settings:
- Advandced Options > Expand Filesystem
- Interface Options > SSH > enable
Reboot pi

7)
Install Java 11 and lighttpd Webserver
- sudo apt install default-jdk
- sudo apt install lighttpd
After that "java -version" shoudl show V11 and unter /var/www/html you can put the Web-Client from https://github.com/xprojects-de/alpdesk-automationclient under Releases
Do not forget to modify the assests/config_dev.json with right IP of Pi and you custom IDs for REST-View
Maybe to upload you have to set "sudo chmod -R 0777 /var/www/html" (not recommended)

8)
create a dir /home/pi/alpdesk
create file startAlpdeskAutomationservice.sh and add following content:

```

#!/bin/sh
cd /home/pi/alpdesk
# Option when database should be stored to USB-Device
# mount /dev/sda1 /media/alpdeskusb/
# sudo mount | grep sda1
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5001 -jar Alpdesk-Automationservice-Deploy.jar /home/pi/alpdesk/alpdesk.properties --spring.profiles.active=prod --spring.datasource.url=jdbc:h2:file:/home/pi/alpdesk/alpdeskdb_prod

```

9)
Make startAlpdeskAutomationservice.sh executeable by "sudo chmod 0777 startAlpdeskAutomationservice.sh"

10)
Upload files to /home/pi/alpdesk you can get from this Repo under Releases and example files under src/main/resources
- Alpdesk-Automationservice-Deploy.jar
- alpdesk.properties (Config-Properties-File)
- alpdesk.xml (Your Device-Config)
Modify you alpdesk.xml and alpdesk.properties as you want

11)
create a file named "autostart-alpdeskservice" under /etc/init.d/ with following content:

```

#! /bin/sh
### BEGIN INIT INFO
# Provides: autostart-alpdeskservice
# Required-Start: $srart
# Required-Stop: $shutdown
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Starting Alpdesk-Service
# Description:
### END INIT INFO
 
case "$1" in
    start)
        echo "starting Alpdesk-Service"
        sh /home/pi/alpdesk/startAlpdeskAutomationservice.sh >> "/home/pi/alpdesk/AlpdeskService.log" 2>&1 &
        ;;
    stop)
        echo "nothing to do"
        ;;
    *)
        echo "Use: /etc/init.d/autostart-alpdeskservice {start|stop}"
        exit 1
        ;;
esac
 
exit 0

```

make autostart-alpdeskservice executable by "sudo chmod 0777 /etc/init.d/autostart-alpdeskservice"

12)
add Sevice for autostart-alpdeskservice
- cd /etc/init.d
- sudo update-rc.d autostart-alpdeskservice defaults
- Reboot (after that the Service shpuld start at boot)

13) OPTIONAL
- Set a static IP by editing /etc/dhcpcd.conf
- Enable OverlayFS and RO /boot by "sudo raspi-config" and OverlayFS
