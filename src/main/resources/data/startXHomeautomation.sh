#!/bin/sh
cd /home/pi/xhomeautomation
mount /dev/sda1 /media/xhomeautomationusb/
sudo mount | grep sda1
java -Xdebug -Xrunjdwp:transport=dt_socket,address=5001,server=y,suspend=n -jar XHomeautomation-Service-Deploy.jar /home/pi/xhomeautomation/homeautomation.properties --spring.profiles.active=prod --spring.datasource.url=jdbc:h2:file:/media/xhomeautomationusb/xhomeautomationdb_prod
